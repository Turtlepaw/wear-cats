package com.turtlepaw.sunlight.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime


@Keep
class LightWorker : Service(), SensorEventListener, ViewModelStoreOwner {
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private lateinit var sunlightViewModel: SunlightViewModel
    override val viewModelStore = ViewModelStore()
    private var timeInLight: Long = 0
    private val lastUpdated: LocalTime = LocalTime.now()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating light listener")
        val channel = NotificationChannel(
            "sunlight",
            "Sunlight",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )

        val notification = NotificationCompat.Builder(this, "sunlight")
            .setSmallIcon(
                IconCompat.createFromIcon(
                    this,
                    android.graphics.drawable.Icon.createWithResource(
                        this,
                        com.turtlepaw.sunlight.R.drawable.sunlight,
                    )
                )!!
            )
            .setLargeIcon(
                android.graphics.drawable.Icon.createWithResource(
                    this,
                    com.turtlepaw.sunlight.R.drawable.sunlight,
                )
            )
            .setContentTitle("Listening for light")
            .setContentText("Listening for changes in light from your device").build()

        startForeground(1, notification)
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
                val timeSinceLastUpdate = Duration.between(lastUpdated, LocalTime.now())
                timeInLight += timeSinceLastUpdate.toMillis()
                // Backwards compatible
                if (timeInLight == 60000L) {
                    CoroutineScope(Dispatchers.Default).launch {
                        Log.d(TAG, "Rewarding 1 minute")
                        sunlightViewModel.addMinute(LocalDate.now())
                        timeInLight = 0
                    }
                }
            } else Log.d(TAG, "Not bright enough (target: $threshold)")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Clean up the sensor and service
        sensorManager!!.unregisterListener(this)
        stopSelf()
    }

    companion object {
        private const val TAG = "LightWorker"
    }
}
