package com.turtlepaw.sunlight.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Keep
class TimeoutReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "TimeoutReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED){
            runBedtime(context, intent)
        }
    }

    private fun getBedtimeState(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, if (Build.MANUFACTURER == "samsung") "setting_bedtime_mode_running_state" else "bedtime_mode") == 1
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update bedtime mode sensor", e)
            false
        }
    }

    private fun runBedtime(context: Context, intent: Intent) {
        Log.d(TAG, "Received bedtime mode change... ($intent)")

        CoroutineScope(Dispatchers.Default).launch {
            // wait for globals to update
            delay(500)
            val state = getBedtimeState(context)
            if(state){
                try {
                    //context.stopService(Intent(context, LightWorker::class.java))
                    context.sendBroadcast(
                        Intent("${context.packageName}.SHUTDOWN_WORKER")
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to stop light worker", e)
                }
            } else {
                try {
//                    context.startForegroundService(Intent(context, LightWorker::class.java))
                    context.sendBroadcast(
                        Intent("${context.packageName}.WAKEUP_WORKER")
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start light worker", e)
                }
            }
        }
    }
}