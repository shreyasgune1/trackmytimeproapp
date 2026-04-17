package com.dreamdevelopersone.trackmytimepro.util

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class TimeUtilsTest {

    @Test
    fun testFormatTime() {
        val cal = Calendar.getInstance()
        cal.set(2024, Calendar.APRIL, 17, 14, 5, 30)
        val ts = cal.timeInMillis
        assertTrue(formatTime(ts).endsWith(":05:30"))
    }

    @Test
    fun testGetStartOfMonthBoundary() {
        val cal = Calendar.getInstance()
        // April 17, 2024
        cal.set(2024, Calendar.APRIL, 17, 10, 0, 0)
        
        // Mocking the "current" time is hard with Calendar.getInstance() inside the function,
        // so we verify the logic qualitatively or by checking the result relative to now.
        val start = getStartOfMonth()
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = start
        
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, startCal.get(Calendar.MINUTE))
    }

    @Test
    fun testGetStartOfYearBoundary() {
        val start = getStartOfYear()
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = start
        
        assertEquals(1, startCal.get(Calendar.DAY_OF_YEAR))
        assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun testFormatDuration() {
        val ms = 3600000L + (30 * 60 * 1000L) + 45000L // 1h 30m 45s
        assertEquals("01:30:45", formatDuration(ms))
        
        val ms2 = 60000L // 1m
        assertEquals("00:01:00", formatDuration(ms2))
        
        val zero = 0L
        assertEquals("00:00:00", formatDuration(zero))
    }

    @Test
    fun testGetStartOfWeekMonday() {
        val monday = getStartOfWeekMonday()
        val cal = Calendar.getInstance()
        cal.timeInMillis = monday
        assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
    }

    @Test
    fun testIsToday() {
        val now = System.currentTimeMillis()
        assertTrue(isToday(now))
        
        val cal = Calendar.getInstance()
        
        // Test yesterday
        cal.add(Calendar.DAY_OF_YEAR, -1)
        assertFalse(isToday(cal.timeInMillis))
        
        // Test tomorrow
        cal.timeInMillis = System.currentTimeMillis()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        assertFalse(isToday(cal.timeInMillis))
        
        // Test start of day today
        val start = getStartOfDay()
        assertTrue(isToday(start))
    }
}
