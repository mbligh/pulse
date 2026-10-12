package com.mbligh.pulse

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ActivityWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("pulse", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val since = prefs.getLong("lastScan", now - 3_600_000L)
        val usage = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = usage.queryEvents(since, now)
        val event = UsageEvents.Event()
        var latest = prefs.getLong("lastActivity", 0L)
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val relevant = event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN ||
                (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                 event.packageName != applicationContext.packageName)
            if (relevant && event.timeStamp > latest) latest = event.timeStamp
        }
        prefs.edit().putLong("lastScan", now).putLong("lastActivity", latest).apply()
        // Safety: deliberately do not send automatic alerts until delivery, consent,
        // missing-permission handling, and alert-state persistence are implemented.
        return Result.success()
    }
}
