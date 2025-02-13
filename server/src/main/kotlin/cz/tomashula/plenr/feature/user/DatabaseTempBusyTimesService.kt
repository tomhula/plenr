package cz.tomashula.plenr.feature.user

import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.feature.user.tempbusytimes.TempBusyTimes
import cz.tomashula.plenr.feature.user.tempbusytimes.TempBusyTimesService
import cz.tomashula.plenr.service.DatabaseService
import cz.tomashula.plenr.util.LocalDateTimePeriod
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
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

        return dbQuery {
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

        dbQuery {
            TempBusyTimeTable.batchInsert(periods) { period ->
                this[TempBusyTimeTable.userId] = caller.id
                this[TempBusyTimeTable.start] = period.start
                this[TempBusyTimeTable.end] = period.end
            }
        }
    }
}
