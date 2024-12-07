package me.tomasan7.plenr.feature.user.preferences

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlin.collections.iterator

@Serializable
class WeeklyTimeRanges private constructor(private val weeklyTimeRanges: Map<DayOfWeek, List<ClosedRange<LocalTime>>>)
{
    fun contains(day: DayOfWeek, time: LocalTime): Boolean
    {
        return weeklyTimeRanges[day]?.any { it.contains(time) } == true
    }

    fun getRangesForDay(day: DayOfWeek) = weeklyTimeRanges[day] ?: emptyList()

    companion object
    {
        fun builder(weeklyTimeRanges: Map<DayOfWeek, List<ClosedRange<LocalTime>>>) = Builder(weeklyTimeRanges)
        fun builder() = Builder(emptyMap())
    }

    class Builder
    {
        private val weeklyTimeRanges = mutableMapOf<DayOfWeek, MutableList<ClosedRange<LocalTime>>>()

        constructor(weeklyTimeRanges: Map<DayOfWeek, List<ClosedRange<LocalTime>>>)
        {
            for (entry in weeklyTimeRanges)
                for (range in entry.value)
                    addTimeRange(entry.key, range)
        }

        /*
        * Generated with ChatGPT: https://chatgpt.com/share/673f6e8f-e908-800e-afaa-29a14c59d6a5
        */
        fun addTimeRange(day: DayOfWeek, timeRange: ClosedRange<LocalTime>)
        {
            val busyRangesForDay = weeklyTimeRanges.getOrPut(day) { mutableListOf() }

            // Find the correct position to insert the range
            val insertionIndex = busyRangesForDay.indexOfFirst { it.start > timeRange.start }.let {
                if (it == -1) busyRangesForDay.size else it
            }
            busyRangesForDay.add(insertionIndex, timeRange)

            // Merge overlapping or contiguous ranges
            val mergedRanges = mutableListOf<ClosedRange<LocalTime>>()
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
        }

        fun addTimeRange(day: DayOfWeek, start: LocalTime, end: LocalTime)
        {
            addTimeRange(day, start..end)
        }

        fun build(): WeeklyTimeRanges
        {
            return WeeklyTimeRanges(weeklyTimeRanges)
        }
    }
}