package cz.tomashula.plenr.util

import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalTimeRangesTest
{

    @Test
    fun testEmptyRanges()
    {
        val ranges = LocalTimeRanges.EMPTY
        assertFalse(ranges.contains(LocalTime(12, 0)))
        assertTrue(ranges.getRanges().isEmpty())
    }

    @Test
    fun testFullRanges()
    {
        val ranges = LocalTimeRanges.FULL
        assertTrue(ranges.contains(LocalTime(0, 0)))
        assertTrue(ranges.contains(LocalTime(12, 0)))
        assertTrue(ranges.contains(LocalTime(23, 59, 59)))
        assertEquals(1, ranges.getRanges().size)
    }

    @Test
    fun testAddRange()
    {
        // Create ranges with two non-overlapping time ranges
        val ranges = LocalTimeRanges.of(
            LocalTime(9, 0)..LocalTime(12, 0),
            LocalTime(14, 0)..LocalTime(17, 0)
        )

        // Test contains
        assertTrue(ranges.contains(LocalTime(10, 0)))
        assertTrue(ranges.contains(LocalTime(15, 0)))
        assertFalse(ranges.contains(LocalTime(13, 0)))

        // Test adding a range that bridges the gap
        val newRanges = ranges.add(LocalTime(11, 0)..LocalTime(15, 0))

        // Should now have one continuous range from 9:00 to 17:00
        assertEquals(1, newRanges.getRanges().size)
        assertTrue(newRanges.contains(LocalTime(13, 0)))
        assertEquals(LocalTime(9, 0), newRanges.getRanges()[0].start)
        assertEquals(LocalTime(17, 0), newRanges.getRanges()[0].endInclusive)
    }

    @Test
    fun testRemoveRange()
    {
        // Create a range from 9:00 to 17:00
        val ranges = LocalTimeRanges.of(LocalTime(9, 0)..LocalTime(17, 0))

        // Remove the middle part
        val newRanges = ranges.remove(LocalTime(12, 0)..LocalTime(14, 0))

        // Should now have two ranges: 9:00-12:00 and 14:00-17:00
        assertEquals(2, newRanges.getRanges().size)
        assertTrue(newRanges.contains(LocalTime(10, 0)))
        assertFalse(newRanges.contains(LocalTime(13, 0)))
        assertTrue(newRanges.contains(LocalTime(15, 0)))
    }

    @Test
    fun testInverted()
    {
        // Create ranges with morning and evening hours
        val ranges = LocalTimeRanges.of(
            LocalTime(9, 0)..LocalTime(12, 0),
            LocalTime(18, 0)..LocalTime(22, 0)
        )

        // Invert the ranges
        val inverted = ranges.inverted()

        // Should now contain times outside the original ranges
        assertFalse(inverted.contains(LocalTime(10, 0)))
        assertFalse(inverted.contains(LocalTime(20, 0)))
        assertTrue(inverted.contains(LocalTime(0, 0)))
        assertTrue(inverted.contains(LocalTime(13, 0)))
        assertTrue(inverted.contains(LocalTime(23, 0)))
    }
    
    @Test
    fun testAddEmptyTimeRange()
    {
        val ranges = LocalTimeRanges.EMPTY
        
        ranges.add(LocalTimeRange.EMPTY)
        assertTrue(ranges.getRanges().isEmpty())
    }
}