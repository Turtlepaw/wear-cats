package com.turtlepaw.sunlight

import android.app.Application
import android.app.NotificationManager
import android.content.IntentFilter
import android.os.PowerManager
import com.turtlepaw.sunlight.services.TimeoutReceiver

open class SunApplication : Application() {
    companion object {
        private const val TAG = "SunlightApplication"
    }
    override fun onCreate() {
        super.onCreate()

        // The following code is from home assistant:
        // https://github.com/home-assistant/android/
        val sensorReceiver = TimeoutReceiver()

        // This will trigger for DND changes, including bedtime and theater mode
        registerReceiver(
            sensorReceiver,
            IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        )

        // This will listen for battery saver
        registerReceiver(
            sensorReceiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        )
    }
}