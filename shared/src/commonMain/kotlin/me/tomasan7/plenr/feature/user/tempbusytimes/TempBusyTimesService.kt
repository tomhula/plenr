package me.tomasan7.plenr.feature.user.tempbusytimes

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import me.tomasan7.plenr.util.LocalDateTimePeriod

@Rpc
interface TempBusyTimesService : RemoteService
{
    /**
     * Allows users to get their temporary busy times for a given period.
     * Allows admins to get temporary busy times for any user for a given period.
     */
    suspend fun getTempBusyTimesForPeriod(userId: Int, period: LocalDateTimePeriod, authToken: String): TempBusyTimes

    /**
     * Allows users to add temporary busy times.
     */
    suspend fun addTempBusyTime(periods: Set<LocalDateTimePeriod>, authToken: String)
}