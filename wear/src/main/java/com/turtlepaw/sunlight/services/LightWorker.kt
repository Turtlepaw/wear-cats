package com.turtlepaw.sunlight.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
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
    private var timeInLight: Int = 0
    private var lastUpdated: LocalTime = LocalTime.now()
    private var threshold: Int? = null
    var context: Context = this
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val thresholdReceiver = ThresholdReceiver()
    private val shutdownReceiver = ShutdownReceiver()
    private val wakeupReceiver = WakeupReceiver()

    // Shared Preferences Listener
    inner class ThresholdReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received new light threshold")
            val defaultThreshold = Settings.SUN_THRESHOLD.getDefaultAsInt()
            // Update threshold value when received a broadcast
            val threshold = intent?.getIntExtra("threshold", defaultThreshold) ?: defaultThreshold
            updateThreshold(threshold)
        }
    }

    inner class ShutdownReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received shutdown command")
            onShutdown()
        }
    }

    inner class WakeupReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received wakeup command")
            onWakeup()
        }
    }

    fun updateThreshold(newThreshold: Int) {
        threshold = newThreshold
        Log.d(TAG, "Threshold updated")
    }

    fun onShutdown() {
        Log.d(TAG, "Shutting down...")
        unregisterReceiver(shutdownReceiver)
        handler.removeCallbacks(runnable)
        sensorManager!!.unregisterListener(this)
    }

    fun onWakeup() {
        Log.d(TAG, "Waking up...")
        val shutDownFilter = IntentFilter("${packageName}.SHUTDOWN_WORKER")
        registerReceiver(shutdownReceiver, shutDownFilter)
        sensorManager!!.registerListener(
            this, lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        handler.postDelayed(runnable, 15000)
    }

    override fun onStart(intent: Intent?, startid: Int) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show()
    }

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

        val thresholdFilter = IntentFilter("${packageName}.THRESHOLD_UPDATED")
        registerReceiver(thresholdReceiver, thresholdFilter)
        val shutDownFilter = IntentFilter("${packageName}.SHUTDOWN_WORKER")
        registerReceiver(shutdownReceiver, shutDownFilter)
        val wakeupFilter = IntentFilter("${packageName}.WAKEUP_WORKER")
        registerReceiver(wakeupReceiver, wakeupFilter)

        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show()

        handler = Handler()
        runnable = Runnable {
            // handler to stop android
            // from hibernating this service
            Log.v(TAG, "Service still running")
            handler.postDelayed(runnable, 10000)
        }

        handler.postDelayed(runnable, 15000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Waiting for light changes")
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        threshold = sharedPreferences.getInt(
            Settings.SUN_THRESHOLD.getKey(),
            Settings.SUN_THRESHOLD.getDefaultAsInt()
        )
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
            val luminance = event.values[0]
            Log.d(TAG, "Received light: $luminance")

            if (threshold != null && luminance >= (threshold ?: 0)) {
                val currentTime = LocalTime.now()
                val timeSinceLastUpdate = Duration.between(lastUpdated, currentTime).toMillis()
                timeInLight += timeSinceLastUpdate.toInt()
                // Backwards compatible
                if (timeInLight >= 60000) {
                    CoroutineScope(Dispatchers.Default).launch {
                        Log.d(TAG, "Rewarding 1 minute")
                        sunlightViewModel.add(LocalDate.now(), (timeInLight / 1000 / 60).toInt())
                        timeInLight = 0
                    }
                }
                lastUpdated = currentTime
            } else {
                Log.d(TAG, "Not bright enough (target: $threshold)")
                lastUpdated = LocalTime.now() // Update lastUpdated even if not bright enough
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
//        sensorManager!!.unregisterListener(this)
//        stopSelf()
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show()
        // Clean up the sensor and service
    }

    companion object {
        private const val TAG = "LightWorker"
    }
}
