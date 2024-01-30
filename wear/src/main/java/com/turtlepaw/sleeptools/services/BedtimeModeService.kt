package com.turtlepaw.sleeptools.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.turtlepaw.sleeptools.services.BedtimeModeListener

class BedtimeModeService : Service() {
    companion object {
        private const val TAG = "BedtimeModeService"
    }
    private val bedtimeModeListener = BedtimeModeListener()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Registering receiver...")
        registerReceiver(
            bedtimeModeListener,
            IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Unregistering receiver...")
        unregisterReceiver(bedtimeModeListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return null if binding is not needed
        return null
    }
}
