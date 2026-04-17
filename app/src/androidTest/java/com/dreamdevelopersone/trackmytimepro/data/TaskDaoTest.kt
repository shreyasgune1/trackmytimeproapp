package com.dreamdevelopersone.trackmytimepro.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRead() = runBlocking {
        val session = TaskSession(name = "Test", startTime = 0, endTime = 100)
        dao.insertSession(session)
        
        val sessions = dao.getAllSessions().first()
        assertEquals(1, sessions.size)
        assertEquals("Test", sessions[0].name)
    }

    @Test
    fun testUpdate() = runBlocking {
        val session = TaskSession(name = "Old", startTime = 0, endTime = 100)
        dao.insertSession(session)
        
        val saved = dao.getAllSessions().first()[0]
        val updated = saved.copy(name = "New")
        dao.updateSession(updated)
        
        val result = dao.getAllSessions().first()[0]
        assertEquals("New", result.name)
    }

    @Test
    fun testDelete() = runBlocking {
        val session = TaskSession(name = "Kill", startTime = 0, endTime = 100)
        dao.insertSession(session)
        
        val saved = dao.getAllSessions().first()[0]
        dao.deleteSession(saved)
        
        val result = dao.getAllSessions().first()
        assertEquals(0, result.size)
    }
}
