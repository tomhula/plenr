package cz.tomashula.plenr.feature.user

import kotlinx.datetime.DayOfWeek
import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.feature.user.preferences.UserPermanentAvailabilityDto
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
) : UserPreferencesService, DatabaseService(database, UserPreferencesTable, UserPermanentAvailabilityTable, TempBusyTimeTable)
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

    override suspend fun getUserPermanentAvailability(
        userId: Int,
        authToken: String
    ): UserPermanentAvailabilityDto
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != userId && !caller.isAdmin)
            throw UnauthorizedException("Only admins can get other users' permanent availability")

        val weeklyTimeRangesBuilder = WeeklyTimeRanges.builder()

        dbQuery {
            UserPermanentAvailabilityTable.selectAll()
                .where { UserPermanentAvailabilityTable.userId eq userId }
                .forEach { row ->
                    val day = row[UserPermanentAvailabilityTable.day]
                    val start = row[UserPermanentAvailabilityTable.start]
                    val end = row[UserPermanentAvailabilityTable.end]
                    weeklyTimeRangesBuilder.addTimeRange(day, start, end)
                }
        }

        return UserPermanentAvailabilityDto(userId, weeklyTimeRangesBuilder.build())
    }

    override suspend fun setUserPermanentAvailability(
        userPermanentAvailabilityDto: UserPermanentAvailabilityDto,
        authToken: String
    )
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        dbQuery {
            UserPermanentAvailabilityTable.deleteWhere { UserPermanentAvailabilityTable.userId eq caller.id }

            for (dayOfWeek in DayOfWeek.entries)
            {
                val dayTimeRanges = userPermanentAvailabilityDto.availableTimes.getRangesForDay(dayOfWeek)

                UserPermanentAvailabilityTable.batchInsert(dayTimeRanges) { timeRange ->
                    this[UserPermanentAvailabilityTable.userId] = userPermanentAvailabilityDto.userId
                    this[UserPermanentAvailabilityTable.day] = dayOfWeek
                    this[UserPermanentAvailabilityTable.start] = timeRange.start
                    this[UserPermanentAvailabilityTable.end] = timeRange.endInclusive
                }
            }
        }
    }
}
