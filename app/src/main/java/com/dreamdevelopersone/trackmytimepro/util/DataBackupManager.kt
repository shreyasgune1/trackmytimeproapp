package com.dreamdevelopersone.trackmytimepro.util

import android.content.Context
import android.net.Uri
import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object DataBackupManager {

    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true 
    }

    suspend fun exportToUri(context: Context, uri: Uri, sessions: List<TaskSession>): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(sessions)
            val stream = context.contentResolver.openOutputStream(uri)
            if (stream == null) return@withContext false
            
            stream.use { os: OutputStream ->
                os.write(jsonString.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importFromUri(context: Context, uri: Uri): List<TaskSession>? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { isStream: InputStream ->
                val jsonString = isStream.bufferedReader().use { it.readText() }
                json.decodeFromString<List<TaskSession>>(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
