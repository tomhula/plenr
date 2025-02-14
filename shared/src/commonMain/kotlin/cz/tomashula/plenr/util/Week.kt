package cz.tomashula.plenr.util

import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import cz.tomashula.plenr.serialization.WeekSerializer
import kotlinx.datetime.format.DateTimeFormat

@Serializable(with = WeekSerializer::class)
class Week(date: LocalDate): Comparable<Week>
{
    val mondayDate = date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)

    val dateRange = mondayDate..mondayDate.plus(6, DateTimeUnit.DAY)
    val dateTimeRange = mondayDate.atTime(LocalTime(0, 0))..mondayDate.plus(7, DateTimeUnit.DAY).atTime(LocalTime(23, 59, 59))
    val period = LocalDateTimePeriod(dateTimeRange.start, dateTimeRange.endInclusive)
    val days by lazy {
        (0 until 7).map { mondayDate.plus(it, DateTimeUnit.DAY) }
    }

    operator fun contains(date: LocalDate) = date in dateRange

    operator fun contains(dateTime: LocalDateTime) = dateTime.date in this

    override fun compareTo(other: Week): Int
    {
        return mondayDate.compareTo(other.mondayDate)
    }

    fun relative(relative: Int): Week
    {
        return Week(mondayDate.plus(relative * 7, DateTimeUnit.DAY))
    }

    fun differenceInWeeks(other: Week): Int
    {
        return (mondayDate.daysUntil(other.mondayDate) / 7).toInt()
    }

    override fun toString() = "${dateRange.start} - ${dateRange.endInclusive}"

    fun toString(format: DateTimeFormat<LocalDate>) = "${dateRange.start.format(format)} - ${dateRange.endInclusive.format(format)}"

    companion object
    {
        fun current() = Week(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }
}
