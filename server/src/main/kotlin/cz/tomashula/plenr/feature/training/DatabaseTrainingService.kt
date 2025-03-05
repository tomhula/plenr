package cz.tomashula.plenr.feature.training

import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.feature.user.UserTable
import cz.tomashula.plenr.feature.user.toUserDto
import cz.tomashula.plenr.mail.MailService
import cz.tomashula.plenr.service.DatabaseService
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.coroutines.CoroutineContext

class DatabaseTrainingService(
    override val coroutineContext: CoroutineContext,
    database: Database,
    serverUrl: String,
    private val authService: AuthService,
    private val mailService: MailService
) : TrainingService, DatabaseService(
    database,
    TrainingTable,
    TrainingParticipantTable
)
{
    private val serverUrl = serverUrl.removeSuffix("/")

    override suspend fun arrangeTrainings(
        trainings: Set<CreateOrUpdateTrainingDto>,
        authToken: String
    )
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can arrange new trainings")

        val trainingsByUser = mutableMapOf<UserDto, MutableSet<CreateOrUpdateTrainingDto>>()
        val participantsByTraining = mutableMapOf<CreateOrUpdateTrainingDto, MutableSet<UserDto>>()
        val newTrainingsIds = mutableMapOf<CreateOrUpdateTrainingDto, Int>()
        val arranger = caller

        dbQuery {
            for (createOrUpdateTrainingDto in trainings)
            {
                val isUpdate = createOrUpdateTrainingDto.id != null
                val trainingId =
                    if (!isUpdate) TrainingTable.insertAndGetId {
                        it[TrainingTable.arrangerId] = arranger.id
                        it[name] = createOrUpdateTrainingDto.name
                        it[description] = createOrUpdateTrainingDto.description
                        it[type] = createOrUpdateTrainingDto.type
                        it[startDateTime] = createOrUpdateTrainingDto.startDateTime
                        it[lengthMinutes] = createOrUpdateTrainingDto.lengthMinutes
                    }.value
                    else
                    {
                        TrainingTable.update({ TrainingTable.id eq createOrUpdateTrainingDto.id }) {
                            it[TrainingTable.arrangerId] = arranger.id
                            it[name] = createOrUpdateTrainingDto.name
                            it[description] = createOrUpdateTrainingDto.description
                            it[type] = createOrUpdateTrainingDto.type
                            it[startDateTime] = createOrUpdateTrainingDto.startDateTime
                            it[lengthMinutes] = createOrUpdateTrainingDto.lengthMinutes
                        }
                        createOrUpdateTrainingDto.id!!
                    }

                if (isUpdate)
                    TrainingParticipantTable.deleteWhere { TrainingParticipantTable.trainingId eq createOrUpdateTrainingDto.id }

                TrainingParticipantTable.batchInsert(createOrUpdateTrainingDto.participantIds) { participantId ->
                    this[TrainingParticipantTable.trainingId] = trainingId
                    this[TrainingParticipantTable.participantId] = participantId
                }

                newTrainingsIds[createOrUpdateTrainingDto] = trainingId

                UserTable
                    .selectAll()
                    .where { UserTable.id inList createOrUpdateTrainingDto.participantIds }
                    .map(ResultRow::toUserDto)
                    .forEach {
                        participantsByTraining.getOrPut(createOrUpdateTrainingDto) { mutableSetOf()  }.add(it)
                        trainingsByUser.getOrPut(it) { mutableSetOf() }.add(createOrUpdateTrainingDto)
                    }
            }
        }

        val trainingsWithParticipantsByUser = trainingsByUser.mapValues {
            it.value.map {
                TrainingWithParticipantsDto(
                    it.id ?: newTrainingsIds[it]!!,
                    arranger,
                    it.name,
                    it.description,
                    it.type,
                    it.startDateTime,
                    it.lengthMinutes,
                    participantsByTraining[it] ?: emptySet()
                )
            }
        }

        for ((user, trainings) in trainingsWithParticipantsByUser)
        {
            val message = MailMessages.getNewTrainingsMessage(arranger, user, trainings.toSet(), serverUrl)
            mailService.sendMail(
                recipient = user.email,
                subject = message.subject,
                body = message.body
            )
        }
    }

    override suspend fun getAllTrainings(
        from: LocalDateTime?,
        to: LocalDateTime?,
        authToken: String
    ): List<TrainingWithParticipantsDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can see all trainings")

        return dbQuery {
            val trainingMap = mutableMapOf<Int, TrainingWithParticipantsDto>()

            val arrangerAlias = UserTable.alias("arranger")
            val participantAlias = UserTable.alias("participant")

            val q = TrainingTable
                .innerJoin(
                    otherTable = arrangerAlias,
                    onColumn = { TrainingTable.arrangerId },
                    otherColumn = { arrangerAlias[UserTable.id] }
                )
                .leftJoin(TrainingParticipantTable)
                .leftJoin(
                    otherTable = participantAlias,
                    onColumn = { TrainingParticipantTable.participantId },
                    otherColumn = { participantAlias[UserTable.id] }
                )
                .selectAll()

            from?.let {
                q.andWhere { TrainingTable.startDateTime greaterEq from }
            }
            to?.let {
                q.andWhere { TrainingTable.startDateTime lessEq to }
            }

            q.forEach { row ->
                val trainingId = row[TrainingTable.id].value
                val arranger = row.toUserDto(arrangerAlias)
                val type = row[TrainingTable.type]
                val participantId = row.getOrNull(participantAlias[UserTable.id])?.value

                val participant = participantId?.let {
                    row.toUserDto(participantAlias)
                }

                val trainingDto = trainingMap.getOrPut(trainingId) {
                    TrainingWithParticipantsDto(
                        id = trainingId,
                        arranger = arranger,
                        name = row[TrainingTable.name],
                        description = row[TrainingTable.description],
                        type = type,
                        startDateTime = row[TrainingTable.startDateTime],
                        lengthMinutes = row[TrainingTable.lengthMinutes],
                        participants = mutableSetOf()
                    )
                }

                if (participant != null)
                    (trainingDto.participants as MutableSet).add(participant)
            }

            trainingMap.values.toList()
        }
    }

    override suspend fun getTrainingsForUser(
        userId: Int,
        from: LocalDateTime?,
        to: LocalDateTime?,
        authToken: String
    ): List<TrainingWithParticipantsDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (userId != caller.id && !caller.isAdmin)
            throw UnauthorizedException("Only admins can see all trainings")

        return dbQuery {
            val trainingMap = mutableMapOf<Int, TrainingWithParticipantsDto>()

            val arrangerAlias = UserTable.alias("arranger")
            val participantAlias = UserTable.alias("participant")

            val relevantTrainingIds = TrainingParticipantTable
                .select(TrainingParticipantTable.trainingId)
                .where { TrainingParticipantTable.participantId eq userId }
                .map { it[TrainingParticipantTable.trainingId].value }
                .toSet()

            if (relevantTrainingIds.isEmpty())
                return@dbQuery emptyList()

            val q = TrainingTable
                .innerJoin(
                    otherTable = arrangerAlias,
                    onColumn = { TrainingTable.arrangerId },
                    otherColumn = { arrangerAlias[UserTable.id] }
                )
                .leftJoin(TrainingParticipantTable)
                .leftJoin(
                    otherTable = participantAlias,
                    onColumn = { TrainingParticipantTable.participantId },
                    otherColumn = { participantAlias[UserTable.id] }
                )
                .selectAll()
                .where { TrainingTable.id inList relevantTrainingIds }

            from?.let {
                q.andWhere { TrainingTable.startDateTime greaterEq from }
            }

            to?.let {
                q.andWhere { TrainingTable.startDateTime lessEq to }
            }

            q.forEach { row ->
                val trainingId = row[TrainingTable.id].value
                val arranger = row.toUserDto(arrangerAlias)
                val type = row[TrainingTable.type]

                val participantId = row.getOrNull(participantAlias[UserTable.id])?.value
                val participant = participantId?.let {
                    row.toUserDto(participantAlias, id = participantId)
                }

                val trainingDto = trainingMap.getOrPut(trainingId) {
                    TrainingWithParticipantsDto(
                        id = trainingId,
                        arranger = arranger,
                        name = row[TrainingTable.name],
                        description = row[TrainingTable.description],
                        type = type,
                        startDateTime = row[TrainingTable.startDateTime],
                        lengthMinutes = row[TrainingTable.lengthMinutes],
                        participants = mutableSetOf()
                    )
                }

                if (participant != null)
                    (trainingDto.participants as MutableSet).add(participant)
            }

            trainingMap.values.toList()
        }
    }
}
