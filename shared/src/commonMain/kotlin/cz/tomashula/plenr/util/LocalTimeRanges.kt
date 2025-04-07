package cz.tomashula.plenr.util

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
class LocalTimeRanges private constructor(private val ranges: List<LocalTimeRange> = listOf())
{
    operator fun contains(time: LocalTime) = ranges.any { it.contains(time) }

    fun getRanges(): List<LocalTimeRange> = ranges

    fun add(range: LocalTimeRange): LocalTimeRanges
    {
        val newRanges = ranges.toMutableList()

        // Find the correct position to insert the range
        val insertionIndex = newRanges.indexOfFirst { it.start > range.start }.let {
            if (it == -1) newRanges.size else it
        }
        newRanges.add(insertionIndex, range)

        // Merge overlapping or contiguous ranges
        val mergedRanges = mutableListOf<LocalTimeRange>()
        for (current in newRanges)
        {
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
        }

        return LocalTimeRanges(mergedRanges)
    }

    fun remove(timeRange: LocalTimeRange): LocalTimeRanges
    {
        val updatedRanges = mutableListOf<LocalTimeRange>()

        for (range in ranges)
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
                range.start < timeRange.start && range.endInclusive >= timeRange.start ->
                {
                    updatedRanges.add(range.start..timeRange.start)
                }
                // Case 4: Overlap at the end
                range.start <= timeRange.endInclusive && range.endInclusive > timeRange.endInclusive ->
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

        return LocalTimeRanges(updatedRanges)
    }
    
    operator fun plus(timeRange: LocalTimeRange): LocalTimeRanges = add(timeRange)
    
    operator fun minus(timeRange: LocalTimeRange): LocalTimeRanges = remove(timeRange)

    fun inverted(): LocalTimeRanges
    {
        var result = FULL
        
        for (range in ranges)
            result = result.remove(range)
        
        return result
    }
    
    fun isEmpty() = ranges.isEmpty() || ranges.all { it.endInclusive < it.start }

    companion object
    {
        val EMPTY = LocalTimeRanges(listOf())
        val FULL = LocalTimeRanges(listOf(LocalTimeRange.FULL))

        fun of(vararg ranges: LocalTimeRange): LocalTimeRanges = of(ranges.asList())

        fun of(ranges: List<LocalTimeRange>): LocalTimeRanges
        {
            var result = EMPTY
            for (range in ranges)
                result = result.add(range)
            
            return result
        }
    }
}
