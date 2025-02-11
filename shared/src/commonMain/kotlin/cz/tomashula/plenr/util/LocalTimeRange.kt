package cz.tomashula.plenr.util

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class LocalTimeRange(
    override val start: LocalTime,
    override val endInclusive: LocalTime
) : ClosedRange<LocalTime>

operator fun LocalTime.rangeTo(other: LocalTime) = LocalTimeRange(this, other)
