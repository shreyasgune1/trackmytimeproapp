package com.dreamdevelopersone.trackmytimepro.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dreamdevelopersone.trackmytimepro.model.TaskSession

@Database(entities = [TaskSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
