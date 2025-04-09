package cz.tomashula.plenr.feature.user

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object BusyPeriodTable : IntIdTable("busy_period")
{
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val start = datetime("start")
    val end = datetime("end")

    init
    {
        check { start less end }
    }
}
