package com.turtlepaw.sleeptools.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.annotation.Keep
import com.turtlepaw.sleeptools.services.BedtimeModeListener

@Keep
class ChargingService : Service() {
    companion object {
        private const val TAG = "ChargingService"
    }
    private val chargingListener = ChargingListener()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Registering receiver...")
        registerReceiver(
            chargingListener,
            IntentFilter(Intent.ACTION_POWER_CONNECTED)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Unregistering the receiver...")
        unregisterReceiver(chargingListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return null if binding is not needed
        return null
    }
}
