package com.naresh.smartcalculatornote

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

object ReminderScheduler {
    const val ACTION_FIRE = "com.naresh.smartcalculatornote.REMINDER_FIRE"
    const val ACTION_SNOOZE = "com.naresh.smartcalculatornote.REMINDER_SNOOZE"
    const val ACTION_DONE = "com.naresh.smartcalculatornote.REMINDER_DONE"
    const val EXTRA_NOTE_ID = "note_id"

    fun schedule(context: Context, note: SmartNote) {
        cancel(context, note.id)
        val at = note.reminderAt ?: return
        if (note.completed) return
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).setAction(ACTION_FIRE).putExtra(EXTRA_NOTE_ID, note.id)
        val pending = PendingIntent.getBroadcast(context, note.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val trigger = at.coerceAtLeast(System.currentTimeMillis() + 1_000L)
        if (Build.VERSION.SDK_INT < 31 || alarm.canScheduleExactAlarms()) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
        } else {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
        }
    }

    fun cancel(context: Context, noteId: String) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).setAction(ACTION_FIRE).putExtra(EXTRA_NOTE_ID, noteId)
        val pending = PendingIntent.getBroadcast(context, noteId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pending)
        pending.cancel()
    }

    fun nextOccurrence(current: Long, repeat: ReminderRepeat, now: Long = System.currentTimeMillis()): Long? {
        if (repeat == ReminderRepeat.NONE) return null
        val zone = ZoneId.systemDefault()
        var next = Instant.ofEpochMilli(current).atZone(zone)
        do {
            next = if (repeat == ReminderRepeat.DAILY) next.plusDays(1) else next.plusWeeks(1)
        } while (next.toInstant().toEpochMilli() <= now)
        return next.toInstant().toEpochMilli()
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val noteId = intent.getStringExtra(ReminderScheduler.EXTRA_NOTE_ID) ?: return@launch
                val repository = CalculatorRepository(context.applicationContext)
                val state = repository.loadOnce()
                val note = state.notes.firstOrNull { it.id == noteId } ?: return@launch
                when (intent.action) {
                    ReminderScheduler.ACTION_DONE -> {
                        ReminderScheduler.cancel(context, noteId)
                        repository.save(state.copy(notes = state.notes.map { if (it.id == noteId) it.copy(completed = true, reminderAt = null, updatedAt = System.currentTimeMillis()) else it }))
                        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(noteId.hashCode())
                    }
                    ReminderScheduler.ACTION_SNOOZE -> {
                        val snoozed = note.copy(completed = false, reminderAt = System.currentTimeMillis() + 10 * 60_000L, updatedAt = System.currentTimeMillis())
                        repository.save(state.copy(notes = state.notes.map { if (it.id == noteId) snoozed else it }))
                        ReminderScheduler.schedule(context, snoozed)
                        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(noteId.hashCode())
                    }
                    else -> {
                        showNotification(context, note)
                        val nextAt = note.reminderAt?.let { ReminderScheduler.nextOccurrence(it, note.repeat) }
                        val updated = note.copy(reminderAt = nextAt, updatedAt = System.currentTimeMillis())
                        repository.save(state.copy(notes = state.notes.map { if (it.id == noteId) updated else it }))
                        if (nextAt != null) ReminderScheduler.schedule(context, updated)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, note: SmartNote) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "smart_note_reminders"
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Smart Note reminders", NotificationManager.IMPORTANCE_HIGH))
        }
        val openIntent = Intent(context, MainActivity::class.java).putExtra(ReminderScheduler.EXTRA_NOTE_ID, note.id)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val openPending = PendingIntent.getActivity(context, note.id.hashCode(), openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        fun actionPending(action: String, offset: Int) = PendingIntent.getBroadcast(
            context, note.id.hashCode() + offset,
            Intent(context, ReminderReceiver::class.java).setAction(action).putExtra(ReminderScheduler.EXTRA_NOTE_ID, note.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(note.title.ifBlank { "Smart Note" })
            .setContentText(note.details.ifBlank { "Reminder" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(note.details))
            .setContentIntent(openPending).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "10 min Snooze", actionPending(ReminderScheduler.ACTION_SNOOZE, 1))
            .addAction(0, "Done", actionPending(ReminderScheduler.ACTION_DONE, 2))
            .build()
        manager.notify(note.id.hashCode(), notification)
    }
}

class ReminderRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = CalculatorRepository(context.applicationContext)
                val state = repository.loadOnce()
                val now = System.currentTimeMillis()
                var changed = false
                val notes = state.notes.map { note ->
                    val at = note.reminderAt
                    if (note.completed || at == null) note else {
                        val corrected = if (at <= now) ReminderScheduler.nextOccurrence(at, note.repeat, now) else at
                        if (corrected != at) changed = true
                        note.copy(reminderAt = corrected)
                    }
                }
                if (changed) repository.save(state.copy(notes = notes))
                notes.filter { !it.completed && it.reminderAt != null }.forEach { ReminderScheduler.schedule(context, it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
