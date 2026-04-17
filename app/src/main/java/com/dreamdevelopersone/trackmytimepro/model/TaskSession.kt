package com.dreamdevelopersone.trackmytimepro.model
 
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "task_sessions")
data class TaskSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var startTime: Long,
    var endTime: Long
) {
    fun duration() = endTime - startTime
}
