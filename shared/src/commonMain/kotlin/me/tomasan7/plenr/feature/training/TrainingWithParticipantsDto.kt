package me.tomasan7.plenr.feature.training

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import me.tomasan7.plenr.feature.user.UserDto

@Serializable
data class TrainingWithParticipantsDto(
    val id: Int,
    val arrangerId: Int,
    val name: String,
    val description: String,
    val type: TrainingType,
    val startDateTime: LocalDateTime,
    val lengthMinutes: Int,
    val participants: List<UserDto>
)