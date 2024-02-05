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

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Received charging change... ($intent)")
        CoroutineScope(Dispatchers.Default).launch {
            runReceiver(context, intent)
        }
    }

    private suspend fun runReceiver(context: Context, intent: Intent) {
        Log.d(tag, "Retrieving new charging state...")
        if(!verifySensor(context, BedtimeSensor.CHARGING)) {
            Log.d(tag, "Charging mode sensor is off")
            return
        }
        val bedtimeViewModel = BedtimeViewModel(context.applicationContext.dataStore)
        val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        // If the watch is charging
        if(status == BatteryManager.BATTERY_STATUS_CHARGING){
            Log.d(tag, "Setting charging bedtime to $status")
            bedtimeViewModel.save(LocalDateTime.now())
        } else {
            Log.d(tag, "Currently not charging")
        }
        Log.d(tag, "Charging state is currently $status")
    }
}