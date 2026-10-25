package com.liferise.app

import com.liferise.app.core.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    @Test
    fun testStartAndEndOfDay() {
        val now = System.currentTimeMillis()
        val start = DateUtils.getStartOfDayMillis(now)
        val end = DateUtils.getEndOfDayMillis(now)

        assertTrue("End of day must be greater than start of day", end > start)
        assertEquals("Difference between start and end of day should be 86399999 ms", 86399999L, end - start)
        assertTrue("Current timestamp must fall between start and end of day", now in start..end)
    }

    @Test
    fun testIsToday() {
        val now = System.currentTimeMillis()
        assertTrue("Current timestamp should be identified as today", DateUtils.isToday(now))

        val yesterday = now - 86400000L
        assertFalse("Timestamp from yesterday should not be today", DateUtils.isToday(yesterday))
    }

    @Test
    fun testIsOverdue() {
        val past = System.currentTimeMillis() - 10000L
        assertTrue("Past timestamp must be recognized as overdue", DateUtils.isOverdue(past))

        val future = System.currentTimeMillis() + 60000L
        assertFalse("Future timestamp must not be overdue", DateUtils.isOverdue(future))
    }

    @Test
    fun testPastDaysWindow() {
        val pastDays = DateUtils.getPastDays(7)
        assertEquals("Must return 7 day timestamps", 7, pastDays.size)
        for (i in 0 until pastDays.size - 1) {
            assertTrue("Days must be in chronological ascending order", pastDays[i] < pastDays[i + 1])
        }
    }
}
