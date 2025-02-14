package cz.tomashula.plenr.feature.user.tempbusytimes

import kotlinx.serialization.Serializable
import cz.tomashula.plenr.util.LocalDateTimePeriod

@Serializable
data class TempBusyTimes(
    val userId: Int,
    val forPeriod: LocalDateTimePeriod,
    val periods: Set<LocalDateTimePeriod>
)
