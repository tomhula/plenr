package cz.tomashula.plenr.feature.user

import kotlinx.datetime.DayOfWeek
import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.feature.user.preferences.PermanentBusyTimesDto
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesDto
import cz.tomashula.plenr.feature.user.preferences.UserPreferencesService
import cz.tomashula.plenr.feature.user.preferences.WeeklyTimeRanges
import cz.tomashula.plenr.service.DatabaseService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.coroutines.CoroutineContext

class DatabaseUserPreferencesService(
    override val coroutineContext: CoroutineContext,
    database: Database,
    private val authService: AuthService
) : UserPreferencesService, DatabaseService(database, UserPreferencesTable, PermanentBusyTimeTable, TempBusyTimeTable)
{
    private fun ResultRow.toUserPreferencesDto() = UserPreferencesDto(
        trainingsPerWeek = this[UserPreferencesTable.trainingsPerWeek],
        trainingArrangedNotiEmail = this[UserPreferencesTable.trainingArrangedNotiEmail],
        trainingArrangedNotiSms = this[UserPreferencesTable.trainingArrangedNotiSms],
        trainingMovedNotiEmail = this[UserPreferencesTable.trainingMovedNotiEmail],
        trainingMovedNotiSms = this[UserPreferencesTable.trainingMovedNotiSms],
        trainingCancelledNotiEmail = this[UserPreferencesTable.trainingCancelledNotiEmail],
        trainingCancelledNotiSms = this[UserPreferencesTable.trainingCancelledNotiSms],
    )

    override suspend fun getUserPreferences(
        userId: Int,
        authToken: String
    ): UserPreferencesDto?
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != userId && !caller.isAdmin)
            throw UnauthorizedException("Only admins can get other users' preferences")

        return dbQuery {
            /* OPTIMIZE: Add limit = 1 to all queries where only one result is expected */
            UserPreferencesTable.selectAll()
                .where { UserPreferencesTable.userId eq userId }
                .singleOrNull()
                ?.toUserPreferencesDto()
        }
    }

    override suspend fun setUserPreferences(
        userId: Int,
        userPreferencesDto: UserPreferencesDto,
        authToken: String
    )
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != userId && !caller.isAdmin)
            throw UnauthorizedException("Only admins can set other users' preferences")

        dbQuery {
            UserPreferencesTable.upsert {
                it[UserPreferencesTable.userId] = userId
                it[trainingsPerWeek] = userPreferencesDto.trainingsPerWeek
                it[trainingArrangedNotiEmail] = userPreferencesDto.trainingArrangedNotiEmail
                it[trainingArrangedNotiSms] = userPreferencesDto.trainingArrangedNotiSms
                it[trainingMovedNotiEmail] = userPreferencesDto.trainingMovedNotiEmail
                it[trainingMovedNotiSms] = userPreferencesDto.trainingMovedNotiSms
                it[trainingCancelledNotiEmail] = userPreferencesDto.trainingCancelledNotiEmail
                it[trainingCancelledNotiSms] = userPreferencesDto.trainingCancelledNotiSms
            }
        }
    }

    override suspend fun getPermanentBusyTimes(
        userId: Int,
        authToken: String
    ): PermanentBusyTimesDto
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != userId && !caller.isAdmin)
            throw UnauthorizedException("Only admins can get other users' permanent busy times")

        val weeklyTimeRangesBuilder = WeeklyTimeRanges.builder()

        dbQuery {
            PermanentBusyTimeTable.selectAll()
                .where { PermanentBusyTimeTable.userId eq userId }
                .forEach { row ->
                    val day = row[PermanentBusyTimeTable.day]
                    val start = row[PermanentBusyTimeTable.start]
                    val end = row[PermanentBusyTimeTable.end]
                    weeklyTimeRangesBuilder.addTimeRange(day, start, end)
                }
        }

        return PermanentBusyTimesDto(userId, weeklyTimeRangesBuilder.build())
    }

    override suspend fun setPermanentBusyTimes(
        permanentBusyTimesDto: PermanentBusyTimesDto,
        authToken: String
    )
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        dbQuery {
            PermanentBusyTimeTable.deleteWhere { PermanentBusyTimeTable.userId eq caller.id }

            for (dayOfWeek in DayOfWeek.entries)
            {
                val dayTimeRanges = permanentBusyTimesDto.busyTimes.getRangesForDay(dayOfWeek)

                PermanentBusyTimeTable.batchInsert(dayTimeRanges) { timeRange ->
                    this[PermanentBusyTimeTable.userId] = permanentBusyTimesDto.userId
                    this[PermanentBusyTimeTable.day] = dayOfWeek
                    this[PermanentBusyTimeTable.start] = timeRange.start
                    this[PermanentBusyTimeTable.end] = timeRange.endInclusive
                }
            }
        }
    }
}
