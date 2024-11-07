package me.tomasan7.plenr.feature.training

import me.tomasan7.plenr.feature.user.UserTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object TrainingTable : IntIdTable("training")
{
    val arrangerId = reference("arranger_id", UserTable)
    val name = varchar("name", 50)
    val description = varchar("description", 255)
    val type = enumeration<TrainingType>("type")
    val startDateTime = datetime("start_date_time")
    val lengthMinutes = integer("length_minutes")
}