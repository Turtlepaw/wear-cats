package com.turtlepaw.sunlight

import android.app.Application

open class SunApplication : Application() {
    companion object {
        private const val TAG = "SunlightApplication"
    }
    override fun onCreate() {
        super.onCreate()

//        val receiver = SensorReceiver()
//        // Start the alarm
//        Log.d(TAG, "Starting sunlight alarm")
//        receiver.startAlarm(this)
    }
}