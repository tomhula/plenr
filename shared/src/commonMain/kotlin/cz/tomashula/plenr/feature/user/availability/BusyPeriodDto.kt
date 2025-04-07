package cz.tomashula.plenr.feature.user.availability

import kotlinx.serialization.Serializable
import cz.tomashula.plenr.util.LocalDateTimePeriod

@Serializable
data class BusyPeriodDto(
    val id: Int,
    val userId: Int,
    val period: LocalDateTimePeriod,
)
