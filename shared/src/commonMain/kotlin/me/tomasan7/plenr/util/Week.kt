package me.tomasan7.plenr.util

import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import me.tomasan7.plenr.serialization.WeekSerializer

@Serializable(with = WeekSerializer::class)
class Week(date: LocalDate)
{
    val mondayDate = date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)

    val dateRange = mondayDate..mondayDate.plus(7, DateTimeUnit.DAY)
    val period = LocalDateTimePeriod(dateRange.start.atTime(LocalTime(0, 0)), dateRange.endInclusive.atTime(LocalTime(23, 59, 59)))
    val days by lazy {
        (0 until 7).map { mondayDate.plus(it, DateTimeUnit.DAY) }
    }

    operator fun contains(date: LocalDate) = date in dateRange

    operator fun contains(dateTime: LocalDateTime) = dateTime.date in this

    companion object
    {
        fun current() = Week(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }
}