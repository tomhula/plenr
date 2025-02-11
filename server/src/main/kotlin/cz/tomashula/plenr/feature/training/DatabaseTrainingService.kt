package cz.tomashula.plenr.feature.training

import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.feature.user.UserTable
import cz.tomashula.plenr.feature.user.toUserDto
import cz.tomashula.plenr.mail.MailService
import cz.tomashula.plenr.service.DatabaseService
import org.jetbrains.exposed.sql.*
import kotlin.coroutines.CoroutineContext

class DatabaseTrainingService(
    override val coroutineContext: CoroutineContext,
    database: Database,
    serverUrl: String,
    private val authService: cz.tomashula.plenr.auth.AuthService,
    private val mailService: MailService
): cz.tomashula.plenr.feature.training.TrainingService, DatabaseService(database,
    cz.tomashula.plenr.feature.training.TrainingTable,
    cz.tomashula.plenr.feature.training.TrainingParticipantTable
)
{
    private val serverUrl = serverUrl.removeSuffix("/")

    override suspend fun createTraining(createTrainingDto: cz.tomashula.plenr.feature.training.CreateTrainingDto, authToken: String): Int
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can create new trainings")

        val trainingId = query {
            val trainingId = cz.tomashula.plenr.feature.training.TrainingTable.insertAndGetId {
                it[cz.tomashula.plenr.feature.training.TrainingTable.arrangerId] = caller.id
                it[cz.tomashula.plenr.feature.training.TrainingTable.name] = createTrainingDto.name
                it[cz.tomashula.plenr.feature.training.TrainingTable.description] = createTrainingDto.description
                it[cz.tomashula.plenr.feature.training.TrainingTable.type] = createTrainingDto.type
                it[cz.tomashula.plenr.feature.training.TrainingTable.startDateTime] = createTrainingDto.startDateTime
                it[cz.tomashula.plenr.feature.training.TrainingTable.lengthMinutes] = createTrainingDto.lengthMinutes
            }.value

            cz.tomashula.plenr.feature.training.TrainingParticipantTable.batchInsert(createTrainingDto.participantIds) { participantId ->
                this[cz.tomashula.plenr.feature.training.TrainingParticipantTable.trainingId] = trainingId
                this[cz.tomashula.plenr.feature.training.TrainingParticipantTable.participantId] = participantId
            }

            trainingId
        }

        val participantsEmails = query {
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

    override suspend fun getAllTrainings(authToken: String): List<cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can see all trainings")

        return query {
            val trainings = mutableMapOf<Int, cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto>()

            cz.tomashula.plenr.feature.training.TrainingTable
                .join(cz.tomashula.plenr.feature.training.TrainingParticipantTable, JoinType.INNER, onColumn = cz.tomashula.plenr.feature.training.TrainingTable.id, otherColumn = cz.tomashula.plenr.feature.training.TrainingParticipantTable.trainingId)
                .join(UserTable, JoinType.INNER, onColumn = cz.tomashula.plenr.feature.training.TrainingParticipantTable.participantId, otherColumn = UserTable.id)
                .selectAll()
                .forEach { resultRow ->
                    val training = trainings.getOrPut(resultRow[cz.tomashula.plenr.feature.training.TrainingTable.id].value) {
                        resultRow.toTrainingWithParticipantsDto(mutableListOf())
                    }
                    val participant = resultRow.toUserDto()
                    (training.participants as MutableList<UserDto>).add(participant)
                }

            trainings.values.toList()
        }
    }

    override suspend fun getTrainingsForUser(userId: Int, authToken: String): List<cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (caller.id != userId && !caller.isAdmin)
            throw UnauthorizedException("You can only see your own trainings")

        return query {
            val trainings = mutableMapOf<Int, cz.tomashula.plenr.feature.training.TrainingWithParticipantsDto>()

            /* OPTIMIZE: Make this a subselect in the following select */
            val trainingsWithUserIds = cz.tomashula.plenr.feature.training.TrainingParticipantTable
                .select(cz.tomashula.plenr.feature.training.TrainingParticipantTable.trainingId)
                .where { cz.tomashula.plenr.feature.training.TrainingParticipantTable.participantId eq userId }
                .map { it[cz.tomashula.plenr.feature.training.TrainingParticipantTable.trainingId] }

            cz.tomashula.plenr.feature.training.TrainingTable
                .join(cz.tomashula.plenr.feature.training.TrainingParticipantTable, JoinType.INNER, onColumn = cz.tomashula.plenr.feature.training.TrainingTable.id, otherColumn = cz.tomashula.plenr.feature.training.TrainingParticipantTable.trainingId)
                .join(UserTable, JoinType.INNER, onColumn = cz.tomashula.plenr.feature.training.TrainingParticipantTable.participantId, otherColumn = UserTable.id)
                .selectAll()
                .where { cz.tomashula.plenr.feature.training.TrainingParticipantTable.trainingId inList trainingsWithUserIds }
                .forEach { resultRow ->
                    val training = trainings.getOrPut(resultRow[cz.tomashula.plenr.feature.training.TrainingTable.id].value) {
                        resultRow.toTrainingWithParticipantsDto(mutableListOf())
                    }
                    val participant = resultRow.toUserDto()
                    (training.participants as MutableList<UserDto>).add(participant)
                }

            trainings.values.toList()
        }
    }
}
