package com.turtlepaw.sunlight.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import java.util.Calendar

@Keep
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Start your alarm here
            startAlarm(context)
        }
    }

    private fun startAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, LightLoggerService::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // Set your alarm schedule here
        // For example, to trigger every 30 minutes:
        val intervalMillis = 30 * 60 * 1000
        val triggerAtMillis = System.currentTimeMillis() + intervalMillis
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            intervalMillis.toLong(),
            pendingIntent
        )

        // Save battery (experimental)
        //scheduleTimeout(context)

        // Initial wake
        context.startService(alarmIntent)
    }

    private fun scheduleTimeout(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, TimeoutReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Calculate the time until the next 8:00 PM
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 20) // 8:00 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= currentTime) {
                add(Calendar.DAY_OF_MONTH, 1) // Move to the next day if the current time has already passed 8:00 PM
            }
        }
        val triggerAtMillis = calendar.timeInMillis

        // Schedule the alarm
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        scheduleWakeup(context, 0)
    }

    private fun scheduleWakeup(context: Context, time: Int){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, BootReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, time)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= currentTime) {
                add(Calendar.DAY_OF_MONTH, 1) // Move to the next day if the current time has already passed 8:00 PM
            }
        }
        val triggerAtMillis = calendar.timeInMillis

        // Schedule the alarm
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}
