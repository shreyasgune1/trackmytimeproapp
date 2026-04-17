package com.dreamdevelopersone.trackmytimepro.data

import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskRepositoryTest {
    private val dao = mockk<TaskDao>()
    private val repository = TaskRepository(dao)

    @Test
    fun testGetAllSessions() = runBlocking {
        val list = listOf(TaskSession(1, "Test", 0, 100))
        every { dao.getAllSessions() } returns flowOf(list)
        
        repository.getAllSessions().collect {
            assertEquals(list, it)
        }
    }

    @Test
    fun testInsert() = runBlocking {
        val session = TaskSession(0, "New", 0, 100)
        coEvery { dao.insertSession(any()) } returns Unit
        
        repository.insertSession(session)
        coVerify { dao.insertSession(session) }
    }

    @Test
    fun testDelete() = runBlocking {
        val session = TaskSession(1, "Del", 0, 100)
        coEvery { dao.deleteSession(any()) } returns Unit
        
        repository.deleteSession(session)
        coVerify { dao.deleteSession(session) }
    }

    @Test
    fun testGetSessionsAfter() = runBlocking {
        val list = listOf(TaskSession(1, "Recent", 1000, 2000))
        every { dao.getSessionsAfter(500L) } returns flowOf(list)
        
        repository.getSessionsAfter(500L).collect {
            assertEquals(list, it)
        }
    }

    @Test
    fun testGetUniqueTaskNames() = runBlocking {
        val names = listOf("Work", "Code")
        every { dao.getUniqueTaskNames() } returns flowOf(names)
        
        repository.getUniqueTaskNames().collect {
            assertEquals(names, it)
        }
    }

    @Test
    fun testUpdateSession() = runBlocking {
        val session = TaskSession(1, "Update", 0, 200)
        coEvery { dao.updateSession(any()) } returns Unit
        
        repository.updateSession(session)
        coVerify { dao.updateSession(session) }
    }
}
