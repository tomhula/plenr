package cz.tomashula.plenr.feature.user

import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.feature.user.availability.BusyPeriodDto
import cz.tomashula.plenr.feature.user.availability.UserAvailabilityService
import cz.tomashula.plenr.feature.user.preferences.UserPermanentAvailabilityDto
import cz.tomashula.plenr.feature.user.preferences.WeeklyTimeRanges
import cz.tomashula.plenr.service.DatabaseService
import cz.tomashula.plenr.util.LocalDateTimePeriod
import cz.tomashula.plenr.util.LocalTimeRanges
import cz.tomashula.plenr.util.now
import cz.tomashula.plenr.util.rangeTo
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.coroutines.CoroutineContext

class DatabaseUserAvailabilityService(
    override val coroutineContext: CoroutineContext,
    database: Database,
    private val authService: AuthService
) : UserAvailabilityService, DatabaseService(database, UserPermanentAvailabilityTable, BusyPeriodTable)
{
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

        if (caller.id != userPermanentAvailabilityDto.userId && !caller.isAdmin)
            throw UnauthorizedException("Only admins can set other users' permanent availability")

        dbQuery {
            UserPermanentAvailabilityTable.deleteWhere { UserPermanentAvailabilityTable.userId eq userPermanentAvailabilityDto.userId }

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

    override suspend fun getBusyPeriodsForUser(
        userId: Int,
        from: LocalDateTime?,
        to: LocalDateTime?,
        authToken: String
    ): List<BusyPeriodDto>
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != userId && !caller.isAdmin)
            throw UnauthorizedException("You can only view your own user data")

        return dbQuery {
            val query = BusyPeriodTable.selectAll()
                .where { BusyPeriodTable.userId eq userId }

            // TODO: Make sure this function returns any periods, that either start, end or continue in the from-to range.
            
            val queryWithFrom = if (from != null) {
                query.andWhere { BusyPeriodTable.start greaterEq from }
            } else {
                query
            }

            val queryWithTo = if (to != null) {
                queryWithFrom.andWhere { BusyPeriodTable.end lessEq to }
            } else {
                queryWithFrom
            }

            queryWithTo.map {
                BusyPeriodDto(
                    id = it[BusyPeriodTable.id].value,
                    userId = it[BusyPeriodTable.userId].value,
                    period = LocalDateTimePeriod(it[BusyPeriodTable.start], it[BusyPeriodTable.end])
                )
            }
        }
    }

    override suspend fun addBusyPeriod(
        busyPeriod: DateTimePeriod,
        authToken: String
    ): Int
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        return dbQuery {
            // Create start and end dates
            val now = LocalDateTime.now()
            val startDateTime = now

            // Calculate end date by adding the DateTimePeriod components to the start date
            val endDateTime = LocalDateTime(
                now.year + busyPeriod.years,
                now.monthNumber + busyPeriod.months,
                now.dayOfMonth + busyPeriod.days,
                now.hour + busyPeriod.hours,
                now.minute + busyPeriod.minutes,
                now.second + busyPeriod.seconds,
                now.nanosecond + busyPeriod.nanoseconds
            )

            val id = BusyPeriodTable.insert {
                it[userId] = caller.id
                it[start] = startDateTime
                it[end] = endDateTime
            } get BusyPeriodTable.id

            id.value
        }
    }

    override suspend fun removeBusyPeriod(
        busyPeriodId: Int,
        authToken: String
    )
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        dbQuery {
            val busyPeriod = BusyPeriodTable.selectAll()
                .where { BusyPeriodTable.id eq busyPeriodId }
                .singleOrNull() ?: return@dbQuery

            if (busyPeriod[BusyPeriodTable.userId].value != caller.id && !caller.isAdmin)
                throw UnauthorizedException("You can only remove your own busy periods")

            BusyPeriodTable.deleteWhere { BusyPeriodTable.id eq busyPeriodId }
        }
    }

    override suspend fun getUserAvailabilityForDay(
        userId: Int,
        date: LocalDate,
        authToken: String
    ): LocalTimeRanges
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (!caller.isAdmin)
            throw UnauthorizedException("Only admins can get user availability for a day")

        // Get permanent availability for the day of week
        val permanentAvailability = getUserPermanentAvailability(userId, authToken)
        val dayOfWeek = date.dayOfWeek
        val dayRanges = permanentAvailability.availableTimes.getRangesForDay(dayOfWeek)
        var availabilityRanges = LocalTimeRanges.of(dayRanges)

        // Get busy periods for the day and subtract them from the availability
        val startOfDay = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 0, 0)
        val endOfDay = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 23, 59, 59, 999_999_999)

        val busyPeriods = getBusyPeriodsForUser(userId, startOfDay, endOfDay, authToken)

        for (busyPeriod in busyPeriods) {
            val start = busyPeriod.period.start.time
            val end = busyPeriod.period.end.time
            availabilityRanges = availabilityRanges.remove(start..end)
        }

        return availabilityRanges
    }

    override suspend fun getUsersAvailabilityForDay(
        userIds: List<Int>,
        date: LocalDate,
        authToken: String
    ): Map<Int, LocalTimeRanges>
    {
        val result = mutableMapOf<Int, LocalTimeRanges>()
        
        for (userId in userIds)
            result[userId] = getUserAvailabilityForDay(userId, date, authToken)
        
        return result.toMap()
    }
}
