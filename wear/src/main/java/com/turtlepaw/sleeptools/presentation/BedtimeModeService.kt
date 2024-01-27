package com.turtlepaw.sleeptools.presentation

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class BedtimeModeService : Service() {
    private val bedtimeModeListener = BedtimeModeListener()

    override fun onCreate() {
        super.onCreate()
        registerReceiver(
            bedtimeModeListener,
            IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bedtimeModeListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return null if binding is not needed
        return null
    }
}
