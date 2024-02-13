package com.turtlepaw.sunlight

import android.app.Application
import com.turtlepaw.sunlight.services.SensorReceiver

open class SunApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val receiver = SensorReceiver()
        // Start the alarm
        receiver.startAlarm(this)
    }
}