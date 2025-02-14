package cz.tomashula.plenr.feature.training

import kotlinx.datetime.LocalDateTime
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface TrainingService : RemoteService
{
    /** Allows admins to arrange new trainings */
    suspend fun arrangeTrainings(
        trainings: Iterable<CreateTrainingDto>,
        authToken: String
    )

    /**
     * Allows admins to see all trainings that start after [from] and before [to].
     * [from] and [to] can be `null`, representing no lower or upper bound.
     * If both are null, all trainings are returned.
     */
    suspend fun getAllTrainings(
        from: LocalDateTime? = null,
        to: LocalDateTime? = null,
        authToken: String
    ): List<TrainingWithParticipantsDto>

    /**
     * Allows users to see their trainings that start after [from] and before [to].
     * Admins can see anyone's trainings.
     * [from] and [to] can be `null`, representing no lower or upper bound.
     * If both are null, all trainings are returned.
     */
    suspend fun getTrainingsForUser(
        userId: Int,
        from: LocalDateTime? = null,
        to: LocalDateTime? = null,
        authToken: String
    ): List<TrainingWithParticipantsDto>
}
