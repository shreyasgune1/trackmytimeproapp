package com.dreamdevelopersone.trackmytimepro.ui

import app.cash.turbine.test
import com.dreamdevelopersone.trackmytimepro.data.TaskRepository
import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlinx.coroutines.delay
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimeViewModelTest {

    private val repository = mockk<TaskRepository>(relaxed = true)
    private lateinit var viewModel: TimeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllSessions() } returns flowOf(emptyList())
        every { repository.getSessionsAfter(any()) } returns flowOf(emptyList())
        every { repository.getUniqueTaskNames() } returns flowOf(emptyList())
        viewModel = TimeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTaskNameUpdate() = runTest {
        viewModel.onTaskNameChange("New Task")
        assertEquals("New Task", viewModel.taskName.value)
    }

    @Test
    fun testFilterChange() = runTest {
        viewModel.onFilterChange(FilterType.WEEK)
        assertEquals(FilterType.WEEK, viewModel.selectedFilter.value)
    }

    @Test
    fun testTaskStatsAggregation() = runTest {
        val testSessions = listOf(
            TaskSession(1, "Work", 1000, 2000), // 1000ms
            TaskSession(2, "Work", 3000, 5000), // 2000ms
            TaskSession(3, "Rest", 6000, 7000)  // 1000ms
        )
        every { repository.getSessionsAfter(any()) } returns flowOf(testSessions)
        
        // Trigger a filter change to refresh the flow
        viewModel.onFilterChange(FilterType.ALL)

        viewModel.taskStats.test {
            val stats = awaitItem()
            assertEquals(3000L, stats["Work"])
            assertEquals(1000L, stats["Rest"])
        }
    }

    @Test
    fun testStartStopTaskUpdatesTicker() = runTest {
        viewModel.onTaskNameChange("Work")
        viewModel.startTask()
        assert(viewModel.activeStartTime.value != null)
        
        viewModel.stopTask()
        advanceUntilIdle()
        assertNull(viewModel.activeStartTime.value)
        coVerify { repository.insertSession(any()) }
    }

    @Test
    fun testTickerUpdatesActiveDuration() = runTest {
        viewModel.onTaskNameChange("Work")
        viewModel.startTask()
        
        // Wait for two ticks (ticker runs every 1000ms)
        delay(2100)
        
        val duration = viewModel.activeDuration.value
        assert(duration >= 2000L)
        
        viewModel.stopTask()
    }

    @Test
    fun testRedundantStartStop() = runTest {
        viewModel.startTask()
        val firstStart = viewModel.activeStartTime.value
        viewModel.startTask() // Should not change start time
        assertEquals(firstStart, viewModel.activeStartTime.value)
        
        viewModel.stopTask()
        viewModel.stopTask() // Should not crash
        coVerify(exactly = 1) { repository.insertSession(any()) }
    }

    @Test
    fun testDeleteSession() = runTest {
        val session = TaskSession(1, "Delete Me", 0, 1000)
        viewModel.deleteSession(session)
        runCurrent()
        coVerify { repository.deleteSession(session) }
    }

    @Test
    fun testUpdateSessionEndTime() = runTest {
        val session = TaskSession(1, "Edit Me", 0, 1000)
        viewModel.updateSessionEndTime(session, 2000L)
        runCurrent()
        coVerify { repository.updateSession(match { it.endTime == 2000L }) }
    }

    @Test
    fun testExportAllData() = runTest {
        val context = mockk<android.content.Context>()
        val uri = mockk<android.net.Uri>()
        var result: Boolean? = null
        
        coEvery { repository.getAllSessions() } returns flowOf(emptyList())
        
        viewModel.exportAllData(context, uri) { result = it }
        runCurrent()
        assertNotNull(result)
    }

    @Test
    fun testImportAllData() = runTest {
        val context = mockk<android.content.Context>()
        val uri = mockk<android.net.Uri>()
        var result: Boolean? = null
        
        viewModel.importAllData(context, uri) { result = it }
        advanceUntilIdle()
    }

    @Test
    fun testAnalyticsWithDummyData() = runTest {
        val now = System.currentTimeMillis()
        val oneHour = 3600000L
        
        // Dummy data spanning various ranges
        val sessions = listOf(
            TaskSession(1, "TodayTask", now - 5000, now - 1000), // Today
            TaskSession(2, "WeekTask", now - (3 * 24 * oneHour), now - (3 * 24 * oneHour - 1000)), // This week
            TaskSession(3, "MonthTask", now - (15 * 24 * oneHour), now - (15 * 24 * oneHour - 1000)), // This month
            TaskSession(4, "YearTask", now - (100 * 24 * oneHour), now - (100 * 24 * oneHour - 1000)), // This year
            TaskSession(5, "OldTask", now - (500 * 24 * oneHour), now - (500 * 24 * oneHour - 1000))  // Over a year ago
        )
        
        // 1. Test ALL (Lifetime)
        every { repository.getSessionsAfter(0L) } returns flowOf(sessions)
        viewModel.onFilterChange(FilterType.ALL)
        viewModel.taskStats.test {
            val stats = awaitItem()
            assertEquals("Lifetime should have all tasks", 5, stats.size)
        }
        
        // 2. Test TODAY
        // For testing TODAY, we need to know what getStartOfDay() returns.
        // Since we can't easily mock top-level getStartOfDay(), we check that TODAY filter triggers a repository call.
        viewModel.onFilterChange(FilterType.TODAY)
        runCurrent()
        coVerify { repository.getSessionsAfter(match { it > 0 }) }
    }
}
