package me.tomasan7.plenr.feature.user

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object TempBusyTimeTable : IntIdTable("temp_busy_time")
{
    val userId = reference("user_id", UserTable)
    val start = datetime("start")
    val end = datetime("end")

    init
    {
        check { start less end }
    }
}