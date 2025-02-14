package cz.tomashula.plenr.feature.training

import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.feature.training.TrainingParticipantTable.trainingId
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.feature.user.UserTable
import cz.tomashula.plenr.feature.user.toUserDto
import cz.tomashula.plenr.mail.MailService
import cz.tomashula.plenr.service.DatabaseService
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
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
        trainings: Iterable<CreateTrainingDto>,
        authToken: String
    )
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can arrange new trainings")

        val participantsEmails = mutableMapOf<Int, String>()

        dbQuery {
            for (createTrainingDto in trainings)
            {
                val trainingId = TrainingTable.insertAndGetId {
                    it[arrangerId] = caller.id
                    it[name] = createTrainingDto.name
                    it[description] = createTrainingDto.description
                    it[type] = createTrainingDto.type
                    it[startDateTime] = createTrainingDto.startDateTime
                    it[lengthMinutes] = createTrainingDto.lengthMinutes
                }.value

                TrainingParticipantTable.batchInsert(createTrainingDto.participantIds) { participantId ->
                    this[TrainingParticipantTable.trainingId] = trainingId
                    this[TrainingParticipantTable.participantId] = participantId
                }

                UserTable
                    .select(UserTable.email)
                    .where { UserTable.id inList createTrainingDto.participantIds }
                    .forEach { row ->
                        participantsEmails[trainingId] = row[UserTable.email]
                    }
            }
        }

        val trainingsByUsers = participantsEmails.entries.associate { (trainingId, participantEmail) ->
            participantEmail to trainings.filter { it.participantIds.contains(trainingId) }
        }

        // TODO: Create properly formatted email
        for ((email, trainings) in trainingsByUsers)
            mailService.sendMail(
                recipient = email,
                subject = if (trainings.size > 1) "New trainings" else "New training",
                body = "You have been added to new trainings: $trainings"
            )
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
                        participants = mutableListOf()
                    )
                }

                if (participant != null)
                    (trainingDto.participants as MutableList).add(participant)
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

            // Alias for arranger and participant
            val arrangerAlias = UserTable.alias("arranger")
            val participantAlias = UserTable.alias("participant")

            // Select only trainings where userId is a participant
            val relevantTrainingIds = TrainingParticipantTable
                .select(TrainingParticipantTable.trainingId)
                .where { TrainingParticipantTable.participantId eq userId }
                .map { it[TrainingParticipantTable.trainingId].value }
                .toSet()

            if (relevantTrainingIds.isEmpty())
                return@dbQuery emptyList()

            val q = (TrainingTable innerJoin arrangerAlias)
                .leftJoin(TrainingParticipantTable)
                .leftJoin(participantAlias) { TrainingParticipantTable.participantId eq participantAlias[UserTable.id] }
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
                        participants = mutableListOf()
                    )
                }

                if (participant != null)
                    (trainingDto.participants as MutableList).add(participant)
            }

            trainingMap.values.toList()
        }
    }


    /*override suspend fun createTraining(createTrainingDto: CreateTrainingDto, authToken: String): Int
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can create new trainings")

        val trainingId = dbQuery {
            val trainingId = TrainingTable.insertAndGetId {
                it[arrangerId] = caller.id
                it[name] = createTrainingDto.name
                it[description] = createTrainingDto.description
                it[type] = createTrainingDto.type
                it[startDateTime] = createTrainingDto.startDateTime
                it[lengthMinutes] = createTrainingDto.lengthMinutes
            }.value

            TrainingParticipantTable.batchInsert(createTrainingDto.participantIds) { participantId ->
                this[TrainingParticipantTable.trainingId] = trainingId
                this[TrainingParticipantTable.participantId] = participantId
            }

            trainingId
        }

        val participantsEmails = dbQuery {
            UserTable
                .select(UserTable.email)
                .where { UserTable.id inList createTrainingDto.participantIds }
                .map { it[UserTable.email] }
        }

        for (participantEmail in participantsEmails)
            mailService.sendMail(
                recipient = participantEmail,
                subject = "New training",
                body = "You have been added to a new training. Check it out at $serverUrl/training/$trainingId"
            )

        return trainingId
    }

    override suspend fun getAllTrainings(authToken: String): List<TrainingWithParticipantsDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can see all trainings")

        return dbQuery {
            val trainings = mutableMapOf<Int, TrainingWithParticipantsDto>()

            TrainingTable
                .join(TrainingParticipantTable, JoinType.INNER, onColumn = TrainingTable.id, otherColumn = TrainingParticipantTable.trainingId)
                .join(UserTable, JoinType.INNER, onColumn = TrainingParticipantTable.participantId, otherColumn = UserTable.id)
                .selectAll()
                .forEach { resultRow ->
                    val training = trainings.getOrPut(resultRow[TrainingTable.id].value) {
                        resultRow.toTrainingWithParticipantsDto(mutableListOf())
                    }
                    val participant = resultRow.toUserDto()
                    (training.participants as MutableList<UserDto>).add(participant)
                }

            trainings.values.toList()
        }
    }

    override suspend fun getTrainingsForUser(userId: Int, authToken: String): List<TrainingWithParticipantsDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (caller.id != userId && !caller.isAdmin)
            throw UnauthorizedException("You can only see your own trainings")

        return dbQuery {
            val trainings = mutableMapOf<Int, TrainingWithParticipantsDto>()

            *//* OPTIMIZE: Make this a subselect in the following select *//*
            val trainingsWithUserIds = TrainingParticipantTable
                .select(TrainingParticipantTable.trainingId)
                .where { TrainingParticipantTable.participantId eq userId }
                .map { it[TrainingParticipantTable.trainingId] }

            TrainingTable
                .join(TrainingParticipantTable, JoinType.INNER, onColumn = TrainingTable.id, otherColumn = TrainingParticipantTable.trainingId)
                .join(UserTable, JoinType.INNER, onColumn = TrainingParticipantTable.participantId, otherColumn = UserTable.id)
                .selectAll()
                .where { TrainingParticipantTable.trainingId inList trainingsWithUserIds }
                .forEach { resultRow ->
                    val training = trainings.getOrPut(resultRow[TrainingTable.id].value) {
                        resultRow.toTrainingWithParticipantsDto(mutableListOf())
                    }
                    val participant = resultRow.toUserDto()
                    (training.participants as MutableList<UserDto>).add(participant)
                }

            trainings.values.toList()
        }
    }*/
}
