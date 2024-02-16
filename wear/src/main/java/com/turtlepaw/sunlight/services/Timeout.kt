package com.turtlepaw.sunlight.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TimeoutReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "TimeoutReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Unregister or cancel the main alarm here
        unregisterMainAlarm(context)
    }

    private fun unregisterMainAlarm(context: Context) {
        Log.d(TAG, "The timeout alarm has been executed, unregister light alarm receivers")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, LightLoggerService::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // Use FLAG_NO_CREATE to get existing PendingIntent or null if it doesn't exist
        )
        pendingIntent?.let {
            alarmManager.cancel(it) // Cancel the main alarm if it exists
            it.cancel() // Cancel the PendingIntent
        }
    }
}