package com.mbligh.pulse

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private var armed = false
    private lateinit var status: TextView
    private val helpAction = Runnable {
        if (armed) {
            armed = false
            status.text = "HELP activated. Preparing location and sharing options."
            AlertFlow.prepare(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 32)
        }
        layout.addView(TextView(this).apply { text = "Pulse"; textSize = 32f })
        status = TextView(this).apply { text = "Prototype: no automatic alerts are enabled."; textSize = 17f }
        layout.addView(status)
        layout.addView(Button(this).apply {
            text = "Grant usage access"
            setOnClickListener { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        })
        layout.addView(Button(this).apply {
            text = "Grant location access"
            setOnClickListener {
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 10)
            }
        })
        layout.addView(Button(this).apply {
            text = "Start hourly activity checks"
            setOnClickListener {
                val ops = getSystemService(APP_OPS_SERVICE) as AppOpsManager
                val permitted = ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
                if (!permitted) {
                    status.text = "Grant usage access first."
                } else {
                    WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork(
                        "pulse-activity", ExistingPeriodicWorkPolicy.KEEP,
                        PeriodicWorkRequestBuilder<ActivityWorker>(1, TimeUnit.HOURS).build())
                    status.text = "Hourly checks enabled. No automatic alerts configured."
                }
            }
        })
        val help = Button(this).apply { text = "HOLD HELP FOR 3 SECONDS"; textSize = 20f }
        help.setOnTouchListener { _: View, event: MotionEvent ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { armed = true; handler.postDelayed(helpAction, 3000); true }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    armed = false; handler.removeCallbacks(helpAction); true
                }
                else -> true
            }
        }
        layout.addView(help)
        setContentView(layout)
    }
    override fun onDestroy() {
        handler.removeCallbacks(helpAction)
        super.onDestroy()
    }
}
