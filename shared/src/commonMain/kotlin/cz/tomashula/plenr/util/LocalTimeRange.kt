package cz.tomashula.plenr.util

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class LocalTimeRange(
    override val start: LocalTime,
    override val endInclusive: LocalTime
) : ClosedRange<LocalTime>
{
    companion object
    {
        // Range, where start is greater than endInclusive, thus it is empty
        val EMPTY = LocalTimeRange(LocalTime(0, 1), LocalTime(0, 0))
        val FULL = LocalTimeRange(LocalTime(0, 0), LocalTime(23, 59, 59, 999_999_999))
    }
}

operator fun LocalTime.rangeTo(other: LocalTime) = LocalTimeRange(this, other)

operator fun LocalTimeRange.contains(other: LocalTimeRange) = other.start >= this.start && other.endInclusive <= this.endInclusive
