package cz.tomashula.plenr.feature.training

import cz.tomashula.plenr.feature.user.UserTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/** Many [users][UserTable] to one [training][TrainingTable] */
object TrainingParticipantTable : Table("training_participant")
{
    val trainingId = reference("training_id", TrainingTable, onDelete = ReferenceOption.CASCADE)
    val participantId = reference("participant_id", UserTable, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(trainingId, participantId)
}
