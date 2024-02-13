package com.turtlepaw.sunlight.services

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.turtlepaw.sunlight.presentation.dataStore
import com.turtlepaw.sunlight.utils.Settings
import com.turtlepaw.sunlight.utils.SettingsBasics
import com.turtlepaw.sunlight.utils.SunlightViewModel
import com.turtlepaw.sunlight.utils.SunlightViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Keep
class LightLoggerService : Service(), SensorEventListener, ViewModelStoreOwner {
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private lateinit var sunlightViewModel: SunlightViewModel
    override val viewModelStore = ViewModelStore()

    override fun onCreate() {
        super.onCreate()
        sunlightViewModel = ViewModelProvider(this, SunlightViewModelFactory(this.dataStore)).get(SunlightViewModel::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Waiting for light changes")
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
//        val factory = SunlightViewModelFactory(this.dataStore)
//        sunlightViewModel = ViewModelProvider(
//            applicationContext as ViewModelStoreOwner,
//            factory
//        )[SunlightViewModel::class.java]
        sensorManager!!.registerListener(
            this, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val sharedPreferences = getSharedPreferences(
                SettingsBasics.SHARED_PREFERENCES.getKey(),
                SettingsBasics.SHARED_PREFERENCES.getMode()
            )
            val threshold = sharedPreferences.getInt(
                Settings.SUN_THRESHOLD.getKey(),
                Settings.SUN_THRESHOLD.getDefaultAsInt()
            )
            val luminance = event.values[0]
            Log.d(TAG, "Received light: $luminance")

            if (luminance >= threshold) {
                CoroutineScope(Dispatchers.Default).launch {
                    Log.d(TAG, "Rewarding 1 minute")
                    sunlightViewModel.addMinute(LocalDate.now())
                }
            } else Log.d(TAG, "Not bright enough (target: $threshold)")

            // Clean up the sensor and service
            sensorManager!!.unregisterListener(this)
            stopSelf()
        }
    }

    companion object {
        private const val TAG = "LightLoggerService"
    }
}
