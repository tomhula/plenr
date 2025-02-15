package cz.tomashula.plenr.feature.training

import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.feature.user.toUserDto
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toTrainingWithParticipantsDto(participants: Set<UserDto>) = TrainingWithParticipantsDto(
    id = this[TrainingTable.id].value,
    arranger = this.toUserDto(),
    name = this[TrainingTable.name],
    description = this[TrainingTable.description],
    type = this[TrainingTable.type],
    startDateTime = this[TrainingTable.startDateTime],
    lengthMinutes = this[TrainingTable.lengthMinutes],
    participants = participants
)
