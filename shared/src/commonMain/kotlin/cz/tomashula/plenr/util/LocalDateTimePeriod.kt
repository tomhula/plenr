package cz.tomashula.plenr.util

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class LocalDateTimePeriod(val start: LocalDateTime, val end: LocalDateTime)
{
    val range = start..end

    fun contains(dateTime: LocalDateTime) = dateTime in range
}
