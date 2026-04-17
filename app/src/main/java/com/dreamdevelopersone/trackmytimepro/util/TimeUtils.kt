package com.dreamdevelopersone.trackmytimepro.util

import java.text.SimpleDateFormat
import java.util.*

fun formatTime(ts: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ts))

fun formatDuration(ms: Long): String {
    val sec = (ms / 1000) % 60
    val min = (ms / (1000 * 60)) % 60
    val hrs = ms / (1000 * 60 * 60)
    return String.format("%02d:%02d:%02d", hrs, min, sec)
}

fun getStartOfDay(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun getStartOfWeekMonday(): Long {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    
    // Loop backward until Monday to guarantee we are at the start of the current week
    while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return cal.timeInMillis
}

fun getStartOfMonth(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    return cal.timeInMillis
}

fun getStartOfYear(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.set(Calendar.DAY_OF_YEAR, 1)
    return cal.timeInMillis
}

fun isToday(ts: Long): Boolean {
    val start = getStartOfDay()
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    val end = cal.timeInMillis
    return ts in start..end
}
