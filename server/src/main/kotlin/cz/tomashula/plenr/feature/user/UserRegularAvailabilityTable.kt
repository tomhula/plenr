package cz.tomashula.plenr.feature.user

import kotlinx.datetime.DayOfWeek
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.time

object UserRegularAvailabilityTable : IntIdTable("user_regular_availability")
{
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val day = enumeration<DayOfWeek>("day")
    val start = time("start")
    val end = time("end")

    init
    {
        check { start less end }
    }
}
