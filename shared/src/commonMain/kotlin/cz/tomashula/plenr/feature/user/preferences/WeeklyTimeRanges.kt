package cz.tomashula.plenr.feature.user.preferences

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import cz.tomashula.plenr.util.LocalTimeRange
import cz.tomashula.plenr.util.rangeTo
import kotlin.collections.iterator

@Serializable
class WeeklyTimeRanges private constructor(private val weeklyTimeRanges: Map<DayOfWeek, List<LocalTimeRange>>)
{
    fun contains(day: DayOfWeek, time: LocalTime): Boolean
    {
        return weeklyTimeRanges[day]?.any { it.contains(time) } == true
    }

    fun getRangesForDay(day: DayOfWeek) = weeklyTimeRanges[day] ?: emptyList()

    fun builder() = Builder(weeklyTimeRanges)

    companion object
    {
        fun builder(weeklyTimeRanges: Map<DayOfWeek, List<LocalTimeRange>>) = Builder(weeklyTimeRanges)
        fun builder() = Builder(emptyMap())
    }

    class Builder
    {
        private val weeklyTimeRanges = mutableMapOf<DayOfWeek, MutableList<LocalTimeRange>>()

        constructor(weeklyTimeRanges: Map<DayOfWeek, List<LocalTimeRange>>)
        {
            for (entry in weeklyTimeRanges)
                for (range in entry.value)
                    addTimeRange(entry.key, range)
        }

        /*
        * Generated with ChatGPT: https://chatgpt.com/share/673f6e8f-e908-800e-afaa-29a14c59d6a5
        */
        fun addTimeRange(day: DayOfWeek, timeRange: LocalTimeRange): Builder
        {
            val busyRangesForDay = weeklyTimeRanges.getOrPut(day) { mutableListOf() }

            // Find the correct position to insert the range
            val insertionIndex = busyRangesForDay.indexOfFirst { it.start > timeRange.start }.let {
                if (it == -1) busyRangesForDay.size else it
            }
            busyRangesForDay.add(insertionIndex, timeRange)

            // Merge overlapping or contiguous ranges
            val mergedRanges = mutableListOf<LocalTimeRange>()
            for (current in busyRangesForDay)
                if (mergedRanges.isEmpty() || mergedRanges.last().endInclusive < current.start)
                {
                    // No overlap, add the current range
                    mergedRanges.add(current)
                }
                else
                {
                    // Overlap or contiguous, merge with the last range
                    val lastRange = mergedRanges.removeAt(mergedRanges.size - 1)
                    mergedRanges.add(lastRange.start..maxOf(lastRange.endInclusive, current.endInclusive))
                }

            // Replace the list with the merged ranges
            weeklyTimeRanges[day] = mergedRanges

            return this
        }

        fun addTimeRange(day: DayOfWeek, start: LocalTime, end: LocalTime)
        {
            addTimeRange(day, start..end)
        }

        fun removeTimeRange(day: DayOfWeek, timeRange: ClosedRange<LocalTime>): Builder
        {
            val busyRangesForDay = weeklyTimeRanges[day] ?: return this

            val updatedRanges = mutableListOf<LocalTimeRange>()

            for (range in busyRangesForDay)
            {
                when
                {
                    // Case 1: No overlap
                    range.endInclusive < timeRange.start || range.start > timeRange.endInclusive ->
                    {
                        updatedRanges.add(range)
                    }
                    // Case 2: The range is split by the timeRange
                    range.start < timeRange.start && range.endInclusive > timeRange.endInclusive ->
                    {
                        updatedRanges.add(range.start..timeRange.start)
                        updatedRanges.add(timeRange.endInclusive..range.endInclusive)
                    }
                    // Case 3: Overlap at the start
                    range.start < timeRange.start && range.endInclusive > timeRange.start ->
                    {
                        updatedRanges.add(range.start..timeRange.start)
                    }
                    // Case 4: Overlap at the end
                    range.start < timeRange.endInclusive && range.endInclusive > timeRange.endInclusive ->
                    {
                        updatedRanges.add(timeRange.endInclusive..range.endInclusive)
                    }
                    // Case 5: Complete overlap (timeRange covers the range completely)
                    else ->
                    {
                        // Do nothing, effectively removing the range
                    }
                }
            }

            // Replace the existing ranges with the updated ranges
            if (updatedRanges.isEmpty())
            {
                weeklyTimeRanges.remove(day)
            }
            else
            {
                weeklyTimeRanges[day] = updatedRanges
            }

            return this
        }


        fun build(): WeeklyTimeRanges
        {
            return WeeklyTimeRanges(weeklyTimeRanges)
        }
    }
}
