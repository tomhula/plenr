package me.tomasan7.plenr.feature.user.tempbusytimes

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import me.tomasan7.plenr.util.LocalDateTimePeriod
import me.tomasan7.plenr.util.Week

@Rpc
interface TempBusyTimesService : RemoteService
{
    suspend fun getTempBusyTimesForPeriod(userId: Int, period: LocalDateTimePeriod): TempBusyTimes
    suspend fun getTempBusyTimesForWeek(userId: Int, week: Week) = getTempBusyTimesForPeriod(userId, week.period)

    suspend fun addTempBusyTime(userId: Int, periods: Set<LocalDateTimePeriod>)
}