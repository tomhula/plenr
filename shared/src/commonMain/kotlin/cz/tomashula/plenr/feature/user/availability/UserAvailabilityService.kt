package cz.tomashula.plenr.feature.user.availability

import cz.tomashula.plenr.feature.user.preferences.UserRegularAvailabilityDto
import cz.tomashula.plenr.util.LocalDateTimePeriod
import cz.tomashula.plenr.util.LocalTimeRanges
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface UserAvailabilityService: RemoteService
{
    /**
     * Allows each user to get their own regular availability.
     */
    suspend fun getUserRegularAvailability(
        userId: Int,
        authToken: String
    ): UserRegularAvailabilityDto

    /**
     * Allows each user to set their own regular availability.
     */
    suspend fun setUserRegularAvailability(
        userRegularAvailabilityDto: UserRegularAvailabilityDto,
        authToken: String
    )

    /**
     * Allows each user to get their own busy periods.
     * Allows admins to get anyone's busy periods.
     */
    suspend fun getBusyPeriodsForUser(
        userId: Int,
        from: LocalDateTime? = null,
        to: LocalDateTime? = null,
        authToken: String
    ): List<BusyPeriodDto>


    /**
     * Allows users to add new busy period for themselves.
     * @return the id of the new busy period.
     */
    suspend fun addBusyPeriod(
        busyPeriod: LocalDateTimePeriod,
        authToken: String
    ): Int

    /**
     * Allows users to remove their own busy period.
     */
    suspend fun removeBusyPeriod(
        busyPeriodId: Int,
        authToken: String
    )

    /**
     * Allows admins to get anyone's availability for a given day.
     * Returns the final availability for the day, taking into account both regular availability and busy periods.
    */
    suspend fun getUserAvailabilityForDay(
        userId: Int,
        date: LocalDate,
        authToken: String
    ): LocalTimeRanges

    /**
     * Same as [getUserAvailabilityForDay], but for multiple users at once.
     */
    suspend fun getUsersAvailabilityForDay(userIds: List<Int>, date: LocalDate, authToken: String): Map<Int, LocalTimeRanges>
}
