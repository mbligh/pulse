package com.mbligh.pulse

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/** Explicitly user-confirmed share-sheet prototype, not unattended emergency delivery. */
object AlertFlow {
    fun prepare(activity: Activity) {
        val client = LocationServices.getFusedLocationProviderClient(activity)
        val token = CancellationTokenSource()
        val handler = Handler(Looper.getMainLooper())
        var completed = false
        fun finish(location: Location?) {
            if (completed || activity.isFinishing) return
            completed = true
            token.cancel()
            val position = if (location == null) "Location unavailable" else {
                val age = (System.currentTimeMillis() - location.time).coerceAtLeast(0) / 1000
                "Location: https://maps.google.com/?q=${location.latitude},${location.longitude}\n" +
                "Accuracy: approximately ${location.accuracy.toInt()} m; fix age: ${age}s"
            }
            val message = "PULSE MANUAL HELP REQUEST\n${position}\n" +
                "Generated: ${java.util.Date()}\nPlease contact me."
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Pulse help request")
                putExtra(Intent.EXTRA_TEXT, message)
            }
            activity.startActivity(Intent.createChooser(send, "Send help request"))
        }
        handler.postDelayed({ finish(null) }, 30_000)
        try {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token)
                .addOnSuccessListener(activity) { finish(it) }
                .addOnFailureListener(activity) {
                    client.lastLocation.addOnSuccessListener(activity) { finish(it) }
                        .addOnFailureListener(activity) { finish(null) }
                }
        } catch (_: SecurityException) { finish(null) }
    }
}
