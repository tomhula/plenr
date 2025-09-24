package cz.tomashula.plenr.feature.user

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.datetime.datetime

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
