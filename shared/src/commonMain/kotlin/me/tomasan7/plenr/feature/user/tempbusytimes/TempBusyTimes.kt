package me.tomasan7.plenr.feature.user.tempbusytimes

import kotlinx.serialization.Serializable
import me.tomasan7.plenr.util.LocalDateTimePeriod

@Serializable
data class TempBusyTimes(
    val userId: Int,
    val forPeriod: LocalDateTimePeriod,
    val periods: Set<LocalDateTimePeriod>
)