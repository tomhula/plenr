package me.tomasan7.plenr.feature.training

import me.tomasan7.plenr.feature.user.UserTable
import org.jetbrains.exposed.sql.Table

/** Many [users][UserTable] to one [training][TrainingTable] */
object TrainingParticipantTable : Table("training_user")
{
    val trainingId = reference("training_id", TrainingTable)
    val participantId = reference("participant_id", UserTable)

    override val primaryKey = PrimaryKey(trainingId, participantId)
}