package com.turtlepaw.sleeptools.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import androidx.annotation.Keep
import com.turtlepaw.sleeptools.common.BaseReceiver
import com.turtlepaw.sleeptools.presentation.dataStore
import com.turtlepaw.sleeptools.utils.BedtimeModeManager
import com.turtlepaw.sleeptools.utils.BedtimeSensor
import com.turtlepaw.sleeptools.utils.BedtimeViewModel
import com.turtlepaw.sleeptools.utils.SettingsBasics
import com.turtlepaw.sleeptools.utils.verifySensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Keep
class ChargingReceiver: BaseReceiver() {
    override val tag = "ChargingReceiver"
    override val sensorType = BedtimeSensor.CHARGING

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Received charging change... ($intent)")
        CoroutineScope(Dispatchers.Default).launch {
            runReceiver(context)
        }
    }

    private suspend fun runReceiver(context: Context) {
        Log.d(tag, "Retrieving new charging state...")
        if(!verifySensor(context, BedtimeSensor.CHARGING)) {
            Log.d(tag, "Charging mode sensor is off")
            return
        }
        val bedtimeViewModel = BedtimeViewModel(context.applicationContext.dataStore)
        // This will only trigger when the watch is
        // plugged in, so we don't need to check
        Log.d(tag, "Saving new entry...")
        bedtimeViewModel.save(LocalDateTime.now())
    }
}