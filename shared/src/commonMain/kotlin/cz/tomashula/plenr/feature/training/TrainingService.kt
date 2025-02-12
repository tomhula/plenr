package cz.tomashula.plenr.feature.training

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface TrainingService : RemoteService
{
    /** Allows admins to create a new training */
    suspend fun createTraining(createTrainingDto: CreateTrainingDto, authToken: String): Int

    /** Allows admins to see all the trainings */
    suspend fun getAllTrainings(authToken: String): List<TrainingWithParticipantsDto>

    /** Allows admins to see trainings for specific user and user to see their own trainings */
    suspend fun getTrainingsForUser(userId: Int, authToken: String): List<TrainingWithParticipantsDto>
}
