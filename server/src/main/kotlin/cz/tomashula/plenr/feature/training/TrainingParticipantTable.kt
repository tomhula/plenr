package cz.tomashula.plenr.feature.training

import cz.tomashula.plenr.feature.user.UserTable
import org.jetbrains.exposed.sql.Table

/** Many [users][UserTable] to one [training][TrainingTable] */
object TrainingParticipantTable : Table("training_participant")
{
    val trainingId = reference("training_id", TrainingTable)
    val participantId = reference("participant_id", UserTable)

    override val primaryKey = PrimaryKey(trainingId, participantId)
}
