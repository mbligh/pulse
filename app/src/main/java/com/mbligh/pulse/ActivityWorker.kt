package com.mbligh.pulse

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        private const val PREFS = "pulse"
        private const val MAX_EVENTS = 100

        fun scan(context: Context): Int {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val since = prefs.getLong("lastScan", now - 3_600_000L).coerceAtMost(now)
            val usage = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val events = usage.queryEvents(since, now)
            val event = UsageEvents.Event()
            var latest = prefs.getLong("lastActivity", 0L)
            val lines = prefs.getString("eventHistory", "").orEmpty().lines().filter { it.isNotBlank() }.toMutableList()
            var count = 0
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val type = when (event.eventType) {
                    UsageEvents.Event.KEYGUARD_HIDDEN -> "UNLOCK"
                    UsageEvents.Event.ACTIVITY_RESUMED -> if (event.packageName != context.packageName) "FOREGROUND" else null
                    else -> null
                }
                if (type != null && event.timeStamp >= since) {
                    count++
                    if (event.timeStamp > latest) latest = event.timeStamp
                    lines.add("${formatter.format(Date(event.timeStamp))}  $type  ${event.packageName ?: "system"}")
                }
            }
            prefs.edit()
                .putLong("lastScan", now)
                .putLong("lastActivity", latest)
                .putInt("lastEventCount", count)
                .putString("eventHistory", lines.takeLast(MAX_EVENTS).joinToString("\n"))
                .apply()
            return count
        }
    }

    override fun doWork(): Result {
        scan(applicationContext)
        return Result.success()
    }
}
