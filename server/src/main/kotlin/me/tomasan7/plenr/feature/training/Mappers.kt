package me.tomasan7.plenr.feature.training

import me.tomasan7.plenr.feature.user.UserDto
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toTrainingWithParticipantsDto(participants: List<UserDto>) = TrainingWithParticipantsDto(
    id = this[TrainingTable.id].value,
    arrangerId = this[TrainingTable.arrangerId].value,
    name = this[TrainingTable.name],
    description = this[TrainingTable.description],
    type = this[TrainingTable.type],
    startDateTime = this[TrainingTable.startDateTime],
    lengthMinutes = this[TrainingTable.lengthMinutes],
    participants = participants
)