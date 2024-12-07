package me.tomasan7.plenr.feature.user

import me.tomasan7.plenr.auth.AuthService
import me.tomasan7.plenr.auth.UnauthorizedException
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
    override val coroutineContext: CoroutineContext,
    private val authService: AuthService
) : DatabaseService(database), TempBusyTimesService
{
    override suspend fun getTempBusyTimesForPeriod(userId: Int, period: LocalDateTimePeriod, authToken: String): TempBusyTimes
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != userId && !caller.isAdmin)
            throw UnauthorizedException("You can only view your own user data")

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

    override suspend fun addTempBusyTime(periods: Set<LocalDateTimePeriod>, authToken: String)
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        query {
            periods.forEach { period ->
                TempBusyTimeTable.insert {
                    it[TempBusyTimeTable.userId] = caller.id
                    it[start] = period.start
                    it[end] = period.end
                }
            }
        }
    }
}