package me.tomasan7.plenr.feature.user

import me.tomasan7.plenr.feature.user.tempbusytimes.TempBusyTimes
import me.tomasan7.plenr.feature.user.tempbusytimes.TempBusyTimesService
import me.tomasan7.plenr.service.DatabaseService
import me.tomasan7.plenr.util.LocalDateTimePeriod
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.coroutines.CoroutineContext

class DatabaseTempBusyTimesService(
    private val database: Database,
    override val coroutineContext: CoroutineContext
) : DatabaseService(database), TempBusyTimesService
{
    override suspend fun getTempBusyTimesForPeriod(userId: Int, period: LocalDateTimePeriod): TempBusyTimes
    {
        return query {
            val periods = TempBusyTimeTable.selectAll()
                .where {
                    (TempBusyTimeTable.userId eq userId) and
                            (TempBusyTimeTable.start greaterEq period.start) and
                            (TempBusyTimeTable.end lessEq period.end)
                }.map {
                    LocalDateTimePeriod(it[TempBusyTimeTable.start], it[TempBusyTimeTable.end])
                }.toSet()

            TempBusyTimes(userId, period, periods)
        }
    }

    override suspend fun addTempBusyTime(userId: Int, periods: Set<LocalDateTimePeriod>)
    {
        query {
            periods.forEach { period ->
                TempBusyTimeTable.insert {
                    it[TempBusyTimeTable.userId] = userId
                    it[start] = period.start
                    it[end] = period.end
                }
            }
        }
    }
}