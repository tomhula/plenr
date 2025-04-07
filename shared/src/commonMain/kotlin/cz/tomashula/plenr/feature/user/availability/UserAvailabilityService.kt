package cz.tomashula.plenr.feature.user.availability

import cz.tomashula.plenr.feature.user.preferences.UserPermanentAvailabilityDto
import cz.tomashula.plenr.util.LocalTimeRanges
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc

@Rpc
interface UserAvailabilityService: RemoteService
{
    /**
     * Allows each user to get their own permanent availability.
     */
    suspend fun getUserPermanentAvailability(
        userId: Int,
        authToken: String
    ): UserPermanentAvailabilityDto

    /**
     * Allows each user to set their own permanent availability.
     */
    suspend fun setUserPermanentAvailability(
        userPermanentAvailabilityDto: UserPermanentAvailabilityDto,
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
     */
    suspend fun addBusyPeriod(
        busyPeriod: DateTimePeriod,
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
     * Returns the final availability for the day, taking into account both permanent availability and busy periods.
    */
    suspend fun getUserAvailabilityForDay(
        userId: Int,
        date: LocalDate,
        authToken: String
    ): LocalTimeRanges
}