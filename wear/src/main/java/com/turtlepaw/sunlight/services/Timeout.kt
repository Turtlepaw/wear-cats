package com.turtlepaw.sunlight.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.Keep

@Keep
class TimeoutReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "TimeoutReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Unregister or cancel the main alarm here
        unregisterMainAlarm(context)
    }

    private fun getIntent(context: Context): Intent {
        return Intent(context, LightWorker::class.java)
    }

    private fun unregisterMainAlarm(context: Context) {
        Log.d(TAG, "The timeout alarm has been executed, unregister light alarm receivers")
        val intent = getIntent(context)
        context.stopService(intent)
        // old with alarm
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val alarmIntent = Intent(context, LightLoggerService::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            0,
//            alarmIntent,
//            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // Use FLAG_NO_CREATE to get existing PendingIntent or null if it doesn't exist
//        )
//        pendingIntent?.let {
//            alarmManager.cancel(it) // Cancel the main alarm if it exists
//            it.cancel() // Cancel the PendingIntent
//        }
    }
}