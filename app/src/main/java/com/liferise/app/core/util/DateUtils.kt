package com.liferise.app.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatToDateString(epochMillis: Long, locale: Locale = Locale.getDefault()): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", locale)
        return sdf.format(Date(epochMillis))
    }

    fun formatToTimeString(epochMillis: Long, locale: Locale = Locale.getDefault()): String {
        val sdf = SimpleDateFormat("hh:mm a", locale)
        return sdf.format(Date(epochMillis))
    }

    fun formatToDateTimeString(epochMillis: Long, locale: Locale = Locale.getDefault()): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", locale)
        return sdf.format(Date(epochMillis))
    }

    fun formatToIsoDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(epochMillis))
    }

    fun getStartOfDayMillis(epochMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDayMillis(epochMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun isToday(epochMillis: Long): Boolean {
        val now = System.currentTimeMillis()
        return epochMillis in getStartOfDayMillis(now)..getEndOfDayMillis(now)
    }

    fun isOverdue(dueEpochMillis: Long): Boolean {
        return dueEpochMillis < System.currentTimeMillis()
    }

    fun getPastDays(count: Int = 7): List<Long> {
        val result = mutableListOf<Long>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = getStartOfDayMillis()
        for (i in (count - 1) downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            result.add(c.timeInMillis)
        }
        return result
    }

    fun getDayOfWeekShort(epochMillis: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }
}
