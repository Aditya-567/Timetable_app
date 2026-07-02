package com.rahul.mobile.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rahul.mobile.data.AppPreferences
import com.rahul.mobile.data.TimetableItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ClassReminderScheduler {
    private const val CHANNEL_ID = "class_reminders"
    private const val PREFS = "class_reminder_prefs"
    private const val KEY_CODES = "scheduled_codes"

    fun syncReminders(
        context: Context,
        items: List<TimetableItem>,
        settings: AppPreferences.NotificationSettings
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancelAllScheduled(context, alarmManager)

        if (!settings.enabled) return
        if (settings.section.equals(AppPreferences.FILTER_ALL, ignoreCase = true)) return

        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val codes = mutableSetOf<String>()

        items.forEach { item ->
            val section = normalizeSection(item.section)
            if (!section.equals(settings.section, ignoreCase = true)) return@forEach

            val date = parseDate(item.date) ?: return@forEach
            if (date.isBefore(today)) return@forEach

            val start = parseStartTime(item.time) ?: return@forEach
            val classDateTime = LocalDateTime.of(date, start)
            val triggerAt = classDateTime.minusMinutes(settings.minutesBefore.toLong())
            if (!triggerAt.isAfter(now)) return@forEach

            val displayTime = start.format(DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH))
            val requestCode = (item.id + item.date + item.time).hashCode()
            val pendingIntent = reminderIntent(
                context = context,
                requestCode = requestCode,
                title = "Your next class is at $displayTime",
                message = "${item.courseName} • ${item.classroom} • ${item.professor}"
            )

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                pendingIntent
            )
            codes += requestCode.toString()
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_CODES, codes)
            .apply()
    }

    private fun cancelAllScheduled(context: Context, alarmManager: AlarmManager) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val oldCodes = prefs.getStringSet(KEY_CODES, emptySet()).orEmpty()
        oldCodes.forEach { codeStr ->
            val code = codeStr.toIntOrNull() ?: return@forEach
            alarmManager.cancel(reminderIntent(context, code, "", ""))
        }
        prefs.edit().remove(KEY_CODES).apply()
    }

    private fun reminderIntent(
        context: Context,
        requestCode: Int,
        title: String,
        message: String
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun parseDate(raw: String): LocalDate? {
        val inputs = listOf(
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEEE, dd MMMM, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ISO_LOCAL_DATE
        )
        inputs.forEach { fmt ->
            val parsed = runCatching { LocalDate.parse(raw.trim(), fmt) }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }

    private fun parseStartTime(timeRange: String): LocalTime? {
        val head = timeRange
            .split(Regex("\\s*[-–—]\\s*"))
            .firstOrNull()
            .orEmpty()
            .trim()
            .replace(" ", "")
            .replace(".", ":")
        val formats = listOf(
            DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        )
        formats.forEach { fmt ->
            val parsed = runCatching { LocalTime.parse(head.uppercase(Locale.ENGLISH), fmt) }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }

    private fun normalizeSection(raw: String): String {
        val value = raw.trim()
        if (value.isBlank()) return ""
        return if ("-" in value) value.substringAfterLast("-").trim().uppercase(Locale.ENGLISH)
        else value.uppercase(Locale.ENGLISH)
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Class Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }
    }

    fun notifyNow(context: Context, title: String, message: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        ensureChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        runCatching {
            manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Class Reminder"
        val message = intent.getStringExtra("message") ?: "Your class starts soon"
        ClassReminderScheduler.notifyNow(context, title, message)
    }
}
