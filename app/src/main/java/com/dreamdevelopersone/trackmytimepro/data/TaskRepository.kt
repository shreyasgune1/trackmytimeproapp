package com.dreamdevelopersone.trackmytimepro.data

import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllSessions(): Flow<List<TaskSession>> = taskDao.getAllSessions()

    fun getSessionsAfter(minStartTime: Long): Flow<List<TaskSession>> = taskDao.getSessionsAfter(minStartTime)

    fun getUniqueTaskNames(): Flow<List<String>> = taskDao.getUniqueTaskNames()

    suspend fun insertSession(session: TaskSession) {
        taskDao.insertSession(session)
    }

    suspend fun deleteSession(session: TaskSession) {
        taskDao.deleteSession(session)
    }

    suspend fun updateSession(session: TaskSession) {
        taskDao.updateSession(session)
    }
}
