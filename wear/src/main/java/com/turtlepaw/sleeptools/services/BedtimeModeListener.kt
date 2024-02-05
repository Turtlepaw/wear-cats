package com.turtlepaw.sleeptools.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import com.turtlepaw.sleeptools.common.BaseReceiver
import com.turtlepaw.sleeptools.presentation.dataStore
import com.turtlepaw.sleeptools.utils.BedtimeModeManager
import com.turtlepaw.sleeptools.utils.BedtimeSensor
import com.turtlepaw.sleeptools.utils.BedtimeViewModel
import com.turtlepaw.sleeptools.utils.verifySensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Keep
class BedtimeModeListener: BaseReceiver() {
    override val tag = "BedtimeModeReceiver"
    override val sensorType = BedtimeSensor.BEDTIME

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Received bedtime mode change... ($intent)")
        CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            runReceiver(context)
        }
    }

    private suspend fun runReceiver(context: Context) {
        Log.d(tag, "Retrieving new bedtime state...")
        if(!verifySensor(context, BedtimeSensor.BEDTIME)) {
            Log.d(tag, "Bedtime mode sensor is off")
            return
        }
        val bedtimeViewModel = BedtimeViewModel(context.applicationContext.dataStore)
        // The following code is from home assistant:
        // https://github.com/home-assistant/android/blob/c6ddca8fdc34d2e7741ec82c04b7d8b8d01d3995/wear/src/main/java/io/homeassistant/companion/android/sensors/BedtimeModeSensorManager.kt#L52
        val state = try {
            Settings.Global.getInt(context.contentResolver, if (Build.MANUFACTURER == "samsung") "setting_bedtime_mode_running_state" else "bedtime_mode") == 1
        } catch (e: Exception) {
            Log.e(tag, "Failed to update bedtime mode sensor", e)
            false
        }

        if(state){
            Log.d(tag, "Bedtime mode is on, adding entry")
            bedtimeViewModel.save(LocalDateTime.now())
        } else Log.d(tag, "Bedtime mode is off, not adding entry")
    }
}