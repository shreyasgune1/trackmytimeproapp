package com.dreamdevelopersone.trackmytimepro.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskSessionTest {

    @Test
    fun testDurationCalculation() {
        val start = 1000L
        val end = 2500L
        val session = TaskSession(name = "Test", startTime = start, endTime = end)
        
        assertEquals(1500L, session.duration())
    }

    @Test
    fun testNegativeDuration() {
        // Start after end (error case)
        val session = TaskSession(name = "Error", startTime = 2000L, endTime = 1000L)
        assertEquals(-1000L, session.duration())
    }
}
