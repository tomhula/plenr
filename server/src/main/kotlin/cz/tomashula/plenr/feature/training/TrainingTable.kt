package cz.tomashula.plenr.feature.training

import cz.tomashula.plenr.feature.user.UserTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.datetime.datetime

object TrainingTable : IntIdTable("training")
{
    val arrangerId = reference("arranger_id", UserTable)
    val name = varchar("name", 50)
    val description = varchar("description", 255)
    val type = enumeration<TrainingType>("type")
    val startDateTime = datetime("start_date_time")
    val lengthMinutes = integer("length_minutes").check { it greater 0 }
    val cancelled = bool("cancelled").default(false)
}
