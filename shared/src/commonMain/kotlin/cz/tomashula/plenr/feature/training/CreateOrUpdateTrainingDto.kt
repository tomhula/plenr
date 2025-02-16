package cz.tomashula.plenr.feature.training

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Is a creation when [id] is `null` and update otherwise.
 */
@Serializable
data class CreateOrUpdateTrainingDto(
    val id: Int? = null,
    val name: String,
    val description: String,
    val type: TrainingType,
    val startDateTime: LocalDateTime,
    val lengthMinutes: Int,
    val participantIds: Set<Int>
)
