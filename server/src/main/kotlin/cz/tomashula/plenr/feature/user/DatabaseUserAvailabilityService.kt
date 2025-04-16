package cz.tomashula.plenr.feature.user

import cz.tomashula.plenr.auth.AuthService
import cz.tomashula.plenr.auth.UnauthorizedException
import cz.tomashula.plenr.feature.user.availability.BusyPeriodDto
import cz.tomashula.plenr.feature.user.availability.UserAvailabilityService
import cz.tomashula.plenr.feature.user.preferences.UserRegularAvailabilityDto
import cz.tomashula.plenr.feature.user.preferences.WeeklyTimeRanges
import cz.tomashula.plenr.service.DatabaseService
import cz.tomashula.plenr.util.LocalDateTimePeriod
import cz.tomashula.plenr.util.LocalTimeRanges
import cz.tomashula.plenr.util.rangeTo
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
) : UserAvailabilityService, DatabaseService(database, UserRegularAvailabilityTable, BusyPeriodTable)
{
    override suspend fun getUserRegularAvailability(
        userId: Int,
        authToken: String
    ): UserRegularAvailabilityDto
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != userId && !caller.isAdmin)
            throw UnauthorizedException("Only admins can get other users' regular availability")

        val weeklyTimeRangesBuilder = WeeklyTimeRanges.builder()

        dbQuery {
            UserRegularAvailabilityTable.selectAll()
                .where { UserRegularAvailabilityTable.userId eq userId }
                .forEach { row ->
                    val day = row[UserRegularAvailabilityTable.day]
                    val start = row[UserRegularAvailabilityTable.start]
                    val end = row[UserRegularAvailabilityTable.end]
                    weeklyTimeRangesBuilder.addTimeRange(day, start, end)
                }
        }

        return UserRegularAvailabilityDto(userId, weeklyTimeRangesBuilder.build())
    }

    override suspend fun setUserRegularAvailability(
        userRegularAvailabilityDto: UserRegularAvailabilityDto,
        authToken: String
    )
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        if (caller.id != userRegularAvailabilityDto.userId && !caller.isAdmin)
            throw UnauthorizedException("Only admins can set other users' regular availability")

        dbQuery {
            UserRegularAvailabilityTable.deleteWhere { UserRegularAvailabilityTable.userId eq userRegularAvailabilityDto.userId }

            for (dayOfWeek in DayOfWeek.entries)
            {
                val dayTimeRanges = userRegularAvailabilityDto.availableTimes.getRangesForDay(dayOfWeek)

                UserRegularAvailabilityTable.batchInsert(dayTimeRanges) { timeRange ->
                    this[UserRegularAvailabilityTable.userId] = userRegularAvailabilityDto.userId
                    this[UserRegularAvailabilityTable.day] = dayOfWeek
                    this[UserRegularAvailabilityTable.start] = timeRange.start
                    this[UserRegularAvailabilityTable.end] = timeRange.endInclusive
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

            // Handle periods that overlap with the specified time range
            val finalQuery = if (from != null && to != null) {
                // Return periods that:
                // 1. Start within the range (start >= from && start <= to)
                // 2. End within the range (end >= from && end <= to)
                // 3. Completely encompass the range (start <= from && end >= to)
                query.andWhere { 
                    (BusyPeriodTable.start lessEq to and (BusyPeriodTable.end greaterEq from))
                }
            } else if (from != null) {
                // If only from is specified, return periods that end after from
                query.andWhere { BusyPeriodTable.end greaterEq from }
            } else if (to != null) {
                // If only to is specified, return periods that start before to
                query.andWhere { BusyPeriodTable.start lessEq to }
            } else {
                query
            }

            finalQuery.map {
                BusyPeriodDto(
                    id = it[BusyPeriodTable.id].value,
                    userId = it[BusyPeriodTable.userId].value,
                    period = LocalDateTimePeriod(it[BusyPeriodTable.start], it[BusyPeriodTable.end])
                )
            }
        }
    }

    override suspend fun addBusyPeriod(
        busyPeriod: LocalDateTimePeriod,
        authToken: String
    ): Int
    {
        val caller = authService.validateToken(authToken) ?: throw UnauthorizedException()

        return dbQuery {
            val id = BusyPeriodTable.insert {
                it[userId] = caller.id
                it[start] = busyPeriod.start
                it[end] = busyPeriod.end
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

        // Get regular availability for the day of week
        val regularAvailability = getUserRegularAvailability(userId, authToken)
        val dayOfWeek = date.dayOfWeek
        val dayRanges = regularAvailability.availableTimes.getRangesForDay(dayOfWeek)
        var availabilityRanges = LocalTimeRanges.of(dayRanges)

        // Get busy periods for the day and subtract them from the availability
        val startOfDay = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 0, 0)
        val endOfDay = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 23, 59, 59, 999_999_999)

        val busyPeriodDtos = getBusyPeriodsForUser(userId, startOfDay, endOfDay, authToken)

        for (busyPeriodDto in busyPeriodDtos) {
            val busyPeriod = busyPeriodDto.period
            // The whole day is in the busy period
            if (busyPeriod.start.date < date && busyPeriod.end.date > date)
                availabilityRanges = LocalTimeRanges.EMPTY
            // The busy period is only during this day
            else if (busyPeriod.start.date == date && busyPeriod.end.date == date)
                availabilityRanges = availabilityRanges.remove(busyPeriod.start.time..busyPeriod.end.time)
            // The busy period starts on this day, but ends in future day
            else if (busyPeriod.start.date == date && busyPeriod.end.date > date)
                availabilityRanges = availabilityRanges.remove(busyPeriod.start.time..endOfDay.time)
            // The busy period starts on past day, but ends today
            else if (busyPeriod.end.date == date && busyPeriod.start.date < date)
                availabilityRanges = availabilityRanges.remove(startOfDay.time..busyPeriod.end.time)
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
