package com.dreamdevelopersone.trackmytimepro.util

import android.content.Context
import android.content.ContentValues
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.dreamdevelopersone.trackmytimepro.model.TaskSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

suspend fun exportPDF(
    context: Context, 
    sessions: List<TaskSession>, 
    filterTitle: String
): android.net.Uri? = withContext(Dispatchers.IO) {
    val pdf = PdfDocument()
    // A4 size: 595 x 842 points
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    val white = Color.parseColor("#FFFFFF")
    val slateLight = Color.parseColor("#F1F5F9") // Ghost White
    val slateHeader = Color.parseColor("#F8FAFC") // Very Light Slate
    val indigo = Color.parseColor("#6366F1") // Vibrant Indigo
    val textMain = Color.parseColor("#0F172A") // Deep Slate/Ink
    val textSec = Color.parseColor("#64748B") // Muted Slate
    val borderCol = Color.parseColor("#E2E8F0")

    val bgPaint = Paint().apply { color = white }
    val headerBgPaint = Paint().apply { color = slateHeader }
    val zebraPaint = Paint().apply { color = slateLight }
    val borderPaint = Paint().apply { color = borderCol; style = Paint.Style.STROKE; strokeWidth = 1f }
    
    val titlePaint = Paint().apply {
        color = textMain
        textSize = 20f
        isFakeBoldText = true
        isAntiAlias = true
    }
    
    val labelPaint = Paint().apply {
        color = textSec
        textSize = 10f
        isAntiAlias = true
    }
    
    val dataPaint = Paint().apply {
        color = textMain
        textSize = 14f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val textPaint = Paint().apply {
        color = textMain
        textSize = 11f
        isAntiAlias = true
    }

    val chartPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    // 0. Background
    canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

    // 1. Header Bar
    canvas.drawRect(0f, 0f, 595f, 90f, headerBgPaint)
    canvas.drawLine(0f, 90f, 595f, 90f, borderPaint)
    canvas.drawText("TRACKMYTIME ANALYTICS", 40f, 35f, titlePaint.apply { textSize = 11f; color = textSec; alpha = 255; isFakeBoldText = false })
    canvas.drawText(filterTitle.uppercase(), 40f, 65f, titlePaint.apply { textSize = 24f; color = textMain })

    val cardRect = RectF(40f, 110f, 555f, 190f)
    canvas.drawRoundRect(cardRect, 12f, 12f, zebraPaint)
    canvas.drawRoundRect(cardRect, 12f, 12f, borderPaint)
    
    val stats = sessions.groupBy { it.name }.mapValues { it.value.sumOf { s -> s.duration() } }
    val totalDuration = stats.values.sum()
    val topTask = stats.maxByOrNull { it.value }?.key ?: "N/A"

    // Card Content
    canvas.drawText("TOTAL PRODUCTIVITY", 60f, 130f, labelPaint)
    canvas.drawText(formatDuration(totalDuration), 60f, 155f, dataPaint)
    
    canvas.drawText("TOP PERFORMER", 300f, 130f, labelPaint)
    canvas.drawText(topTask, 300f, 155f, dataPaint)

    var y = 210f
    if (stats.isNotEmpty() && totalDuration > 0) {
        // 3. Modern Donut Chart
        canvas.drawText("TASK ALLOCATION", 40f, y, dataPaint.apply { textSize = 12f; color = textSec })
        y += 30f
        
        val chartRect = RectF(60f, y, 240f, y + 180f)
        val colors = listOf("#4F46E5", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#06B6D4")
        var startAngle = -90f
        
        stats.entries.sortedByDescending { it.value }.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value.toFloat() / totalDuration) * 360f
            chartPaint.color = Color.parseColor(colors[index % colors.size])
            canvas.drawArc(chartRect, startAngle, sweepAngle, true, chartPaint)
            
            // Legend
            val legendX = 280f
            val legendY = y + 20f + (index * 25f)
            canvas.drawRoundRect(RectF(legendX, legendY - 10f, legendX + 12f, legendY + 2f), 4f, 4f, chartPaint)
            val percent = (entry.value.toFloat() / totalDuration * 100).toInt()
            canvas.drawText("${entry.key} ($percent%)", legendX + 22f, legendY, textPaint)
            
            startAngle += sweepAngle
        }
        
        // The Donut Hole
        val holePaint = Paint().apply { color = white; isAntiAlias = true }
        canvas.drawCircle(150f, y + 90f, 55f, holePaint)
        
        y += 200f
    }

    // 4. Detailed Session Table
    canvas.drawText("SESSION LOG", 40f, y, dataPaint.apply { textSize = 12f; color = textSec })
    y += 20f
    
    // Table Headers
    val headerPaint = Paint().apply { color = textMain; textSize = 10f; isFakeBoldText = true; isAntiAlias = true }
    canvas.drawRect(40f, y, 555f, y + 24f, headerBgPaint)
    canvas.drawRect(40f, y, 555f, y + 24f, borderPaint)
    canvas.drawText("TASK NAME", 50f, y + 16f, headerPaint)
    canvas.drawText("TIMEFRAME", 210f, y + 16f, headerPaint)
    canvas.drawText("DURATION", 460f, y + 16f, headerPaint)
    y += 24f

    sessions.take(15).forEachIndexed { index, it ->
        if (index % 2 == 1) {
            canvas.drawRect(40f, y, 555f, y + 22f, zebraPaint)
        }
        canvas.drawText(it.name, 50f, y + 15f, textPaint)
        canvas.drawText("${formatTime(it.startTime)} - ${formatTime(it.endTime)}", 210f, y + 15f, textPaint.apply { color = textSec })
        canvas.drawText(formatDuration(it.duration()), 460f, y + 15f, textPaint.apply { color = indigo; isFakeBoldText = true })
        y += 22f
    }

    // Footer
    val footerPaint = Paint().apply { color = textSec; textSize = 9f; isAntiAlias = true; textAlign = Paint.Align.CENTER }
    canvas.drawText("Generated by TrackMyTime Pro • ${formatTime(System.currentTimeMillis())}", 595f / 2f, 820f, footerPaint)

    pdf.finishPage(page)

    // Save using MediaStore for Public Downloads
    val fileName = "TrackMyTime_${filterTitle.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
    val resolver = context.contentResolver
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/TrackMyTime")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { os ->
                pdf.writeTo(os)
            }
        }
        pdf.close()
        return@withContext uri
    } else {
        @Suppress("DEPRECATION")
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appFolder = java.io.File(downloadDir, "TrackMyTime")
        if (!appFolder.exists()) {
            appFolder.mkdirs()
        }
        val file = java.io.File(appFolder, fileName)
        file.outputStream().use { os ->
            pdf.writeTo(os)
        }
        pdf.close()
        return@withContext android.net.Uri.fromFile(file)
    }
}
