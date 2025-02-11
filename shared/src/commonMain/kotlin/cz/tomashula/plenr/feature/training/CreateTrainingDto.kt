package cz.tomashula.plenr.feature.training

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateTrainingDto(
    val name: String,
    val description: String,
    val type: TrainingType,
    val startDateTime: LocalDateTime,
    val lengthMinutes: Int,
    val participantIds: List<Int>
)
