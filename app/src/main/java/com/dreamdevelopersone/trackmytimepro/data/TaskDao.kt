package com.dreamdevelopersone.trackmytimepro.data

import androidx.room.*
import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TaskSession>>

    @Query("SELECT * FROM task_sessions WHERE startTime >= :minStartTime ORDER BY startTime DESC")
    fun getSessionsAfter(minStartTime: Long): Flow<List<TaskSession>>

    @Query("SELECT DISTINCT name FROM (SELECT * FROM task_sessions ORDER BY startTime DESC) LIMIT 10")
    fun getUniqueTaskNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TaskSession)

    @Delete
    suspend fun deleteSession(session: TaskSession)

    @Update
    suspend fun updateSession(session: TaskSession)
}
