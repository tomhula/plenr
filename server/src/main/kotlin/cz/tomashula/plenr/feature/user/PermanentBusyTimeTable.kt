package cz.tomashula.plenr.feature.user

import kotlinx.datetime.DayOfWeek
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.time

object PermanentBusyTimeTable : IntIdTable("permanent_busy_time")
{
    val userId = reference("user_id", UserTable)
    val day = enumeration<DayOfWeek>("day")
    val start = time("start")
    val end = time("end")

    init
    {
        check { start less end }
    }
}
