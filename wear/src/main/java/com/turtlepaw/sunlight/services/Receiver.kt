package com.turtlepaw.sunlight.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Keep
import com.turtlepaw.sunlight.utils.Settings
import com.turtlepaw.sunlight.utils.SettingsBasics
import java.time.LocalTime
import java.time.format.DateTimeParseException
import java.util.Calendar

@Keep
class SensorReceiver : BroadcastReceiver() {
    private val tag = "SensorReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(tag, "Received boot and got action $action")
        // Start your alarm here
        startAlarm(context)
    }

    fun startAlarm(context: Context) {
        Log.d(tag, "Sunlight alarm start confirmed")

        // Save battery (experimental)
        // # Dev Note: why we disabled this? #
        // we'll have to figure out something for
        // this since we can't start services in the background
        // and battery isn't great with it always on
        //scheduleTimeout(context)

        // Start service
        context.startService(Intent(context, LightWorker::class.java))
    }

    private fun scheduleTimeout(context: Context) {
        Log.d(tag, "Scheduling light timeout")
        val sharedPreferences = context.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )

        val timeout = sharedPreferences.getLocalTime(
            Settings.TIMEOUT.getKey(),
            Settings.TIMEOUT.getDefaultAsLocalTime()
        )

        val wakeUp = sharedPreferences.getLocalTime(
            Settings.WAKEUP.getKey(),
            Settings.WAKEUP.getDefaultAsLocalTime()
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, TimeoutReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Calculate the time until the next 8:00 PM
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, timeout.hour) // 8:00 PM
            set(Calendar.MINUTE, timeout.minute)
            set(Calendar.SECOND, timeout.second)
//            if (timeInMillis <= currentTime) {
//                add(Calendar.DAY_OF_MONTH, 1) // Move to the next day if the current time has already passed 8:00 PM
//            }
        }
        val triggerAtMillis = calendar.timeInMillis

        // Schedule the alarm
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        scheduleWakeup(context, wakeUp)
    }

    private fun scheduleWakeup(context: Context, time: LocalTime){
        Log.d(tag, "Scheduling wakeup")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, SensorReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, time.second)
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

private fun SharedPreferences.getLocalTime(key: String, default: LocalTime): LocalTime {
    val timeString = this.getString(key, null) ?: return default

    return try {
        LocalTime.parse(timeString)
    } catch (e: DateTimeParseException) {
        default
    }
}
