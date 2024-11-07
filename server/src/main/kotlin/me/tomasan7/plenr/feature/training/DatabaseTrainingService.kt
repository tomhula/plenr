package me.tomasan7.plenr.feature.training

import me.tomasan7.plenr.auth.AuthService
import me.tomasan7.plenr.auth.UnauthorizedException
import me.tomasan7.plenr.feature.user.UserDto
import me.tomasan7.plenr.feature.user.UserTable
import me.tomasan7.plenr.feature.user.toUserDto
import me.tomasan7.plenr.mail.MailService
import me.tomasan7.plenr.service.DatabaseService
import org.jetbrains.exposed.sql.*
import kotlin.coroutines.CoroutineContext

class DatabaseTrainingService(
    override val coroutineContext: CoroutineContext,
    database: Database,
    serverUrl: String,
    private val authService: AuthService,
    private val mailService: MailService
): TrainingService, DatabaseService(database, TrainingTable, TrainingParticipantTable)
{
    private val serverUrl = serverUrl.removeSuffix("/")

    override suspend fun createTraining(createTrainingDto: CreateTrainingDto, authToken: String): Int
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can create new trainings")

        val trainingId = query {
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

    override suspend fun getAllTrainings(authToken: String): List<TrainingWithParticipantsDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()
        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can see all trainings")

        return query {
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

        return query {
            val trainings = mutableMapOf<Int, TrainingWithParticipantsDto>()

            TrainingTable
                .join(TrainingParticipantTable, JoinType.INNER, onColumn = TrainingTable.id, otherColumn = TrainingParticipantTable.trainingId)
                .join(UserTable, JoinType.INNER, onColumn = TrainingParticipantTable.participantId, otherColumn = UserTable.id)
                .selectAll()
                .where { TrainingParticipantTable.participantId eq userId }
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
}