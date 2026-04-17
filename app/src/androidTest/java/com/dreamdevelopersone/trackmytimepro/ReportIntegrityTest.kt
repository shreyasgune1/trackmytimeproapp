package com.dreamdevelopersone.trackmytimepro

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import androidx.test.platform.app.InstrumentationRegistry
import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import com.dreamdevelopersone.trackmytimepro.util.exportPDF
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReportIntegrityTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        PDFBoxResourceLoader.init(context)
    }

    @Test
    fun testReportTextAndVisualIntegrity() {
        runBlocking {
            // 1. Prepare unique test data
            val taskName = "CheckIntegrity_99"
            // 100% of time for 'CheckIntegrity_99' to make CV easier (solid circle)
            val sessions = listOf(
                TaskSession(1, taskName, 1000, 5000) 
            )

            // 2. Export PDF
            val uri = exportPDF(context, sessions, "Integrity Filter")
            assertTrue("PDF URI should not be null", uri != null)

            // 3. TEXT EXTRACTION (Combined with CV Phase)
            val pfd = context.contentResolver.openFileDescriptor(uri!!, "r")
            assertTrue("FileDescriptor should not be null", pfd != null)
            
            pfd?.use { fd ->
                // Phase A: PDFBox for Text Accuracy
                PDDocument.load(context.contentResolver.openInputStream(uri)).use { doc ->
                    val stripper = PDFTextStripper()
                    val text = stripper.getText(doc)
                    
                    assertTrue("Report should contain title", text.contains("TrackMyTime Pro"))
                    assertTrue("Report should contain task name", text.contains(taskName))
                    assertTrue("Report should contain filter label", text.contains("Integrity Filter"))
                }

                // Phase B: Computer Vision (Pixel Analysis) for Visual Accuracy
                val renderer = PdfRenderer(fd)
                val page = renderer.openPage(0)
                
                // Render to Bitmap
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                // The Donut Chart is centered at x=150, y=(240 + 90) = 330
                // Outer radius is 90, hole radius is 50.
                // Sample a pixel at radius 70 to hit the colored arc.
                val sampleX = 150 + 50 // roughly 200
                val sampleY = 330
                
                val pixelColor = bitmap.getPixel(sampleX, sampleY)
                
                // The first task "IntegrityWork" (or CheckIntegrity_99) should use the first color in our new palette: #4F46E5 (Indigo)
                val expectedColor = Color.parseColor("#4F46E5")
                
                // Verify that the chart is actually drawn with its new professional palette color
                assertEquals("Donut chart pixel color should match premium indigo color", expectedColor, pixelColor)
                
                page.close()
                renderer.close()
            }
        }
    }
}
