package cz.tomashula.plenr.feature.user.preferences

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import cz.tomashula.plenr.util.LocalTimeRange
import cz.tomashula.plenr.util.LocalTimeRanges
import cz.tomashula.plenr.util.rangeTo
import kotlin.collections.iterator

@Serializable
class WeeklyTimeRanges private constructor(private val weeklyTimeRanges: Map<DayOfWeek, LocalTimeRanges>)
{
    fun contains(day: DayOfWeek, time: LocalTime): Boolean = weeklyTimeRanges[day]?.contains(time) == true

    fun getRangesForDay(day: DayOfWeek) = weeklyTimeRanges[day]?.getRanges() ?: emptyList()

    fun builder() = Builder(weeklyTimeRanges)

    fun inverted() = builder().apply {
        for (day in DayOfWeek.entries)
            addTimeRange(day, LocalTimeRange.FULL)

        for ((day, ranges) in weeklyTimeRanges)
            for (range in ranges.getRanges())
                removeTimeRange(day, range)
    }.build()

    companion object
    {
        fun builder() = Builder(emptyMap<DayOfWeek, LocalTimeRanges>())
    }

    class Builder
    {
        private val weeklyTimeRanges = mutableMapOf<DayOfWeek, LocalTimeRanges>()

        constructor(weeklyTimeRanges: Map<DayOfWeek, LocalTimeRanges>)
        {
            for (entry in weeklyTimeRanges)
                this.weeklyTimeRanges[entry.key] = entry.value
        }

        fun addTimeRange(day: DayOfWeek, timeRange: LocalTimeRange): Builder
        {
            val ranges = weeklyTimeRanges.getOrPut(day) { LocalTimeRanges.EMPTY }
            weeklyTimeRanges[day] = ranges.add(timeRange)
            return this
        }

        fun addTimeRange(day: DayOfWeek, start: LocalTime, end: LocalTime)
        {
            addTimeRange(day, start..end)
        }

        fun removeTimeRange(day: DayOfWeek, timeRange: LocalTimeRange): Builder
        {
            val ranges = weeklyTimeRanges[day] ?: return this

            weeklyTimeRanges[day] = ranges.remove(timeRange)

            // Remove the entry if the ranges are empty
            if (weeklyTimeRanges[day]?.isEmpty() == true)
                weeklyTimeRanges.remove(day)

            return this
        }

        fun build(): WeeklyTimeRanges
        {
            return WeeklyTimeRanges(weeklyTimeRanges)
        }
    }
}
