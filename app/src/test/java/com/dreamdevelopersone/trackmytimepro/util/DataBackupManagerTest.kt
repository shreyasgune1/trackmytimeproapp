package com.dreamdevelopersone.trackmytimepro.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class DataBackupManagerTest {

    private val context = mockk<Context>()
    private val contentResolver = mockk<ContentResolver>()
    private val uri = mockk<Uri>()

    @Test
    fun testExportAndImportCycle() = runTest {
        val testSessions = listOf(
            TaskSession(1, "Task A", 1000, 2000),
            TaskSession(2, "Task B", 3000, 4000)
        )
        
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openOutputStream(uri) } returns outputStream

        // 1. Export
        val success = DataBackupManager.exportToUri(context, uri, testSessions)
        assert(success)
        
        val exportedString = outputStream.toString()
        assert(exportedString.contains("Task A"))

        // 2. Import
        val inputStream = ByteArrayInputStream(exportedString.toByteArray())
        every { contentResolver.openInputStream(uri) } returns inputStream

        val importedSessions = DataBackupManager.importFromUri(context, uri)
        assertNotNull(importedSessions)
        assertEquals(2, importedSessions!!.size)
        assertEquals("Task A", importedSessions[0].name)
    }

    @Test
    fun testImportCorruptedJson() = runTest {
        val corruptedJson = "{ not a valid json ]"
        val inputStream = ByteArrayInputStream(corruptedJson.toByteArray())
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(uri) } returns inputStream

        val result = DataBackupManager.importFromUri(context, uri)
        assertEquals(null, result)
    }

    @Test
    fun testExportFailureOnNullStream() = runTest {
        every { context.contentResolver } returns contentResolver
        every { contentResolver.openOutputStream(uri) } returns null
        
        val success = DataBackupManager.exportToUri(context, uri, emptyList())
        assertEquals(false, success)
    }
}
