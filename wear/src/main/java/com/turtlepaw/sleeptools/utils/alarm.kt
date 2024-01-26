package com.turtlepaw.sleeptools.utils

/*
    This file includes code derived from home-assistant/android (https://github.com/home-assistant/android/blob/master/LICENSE.md)
    The original code is licensed under the Apache License Version 2.0
 */

import android.app.AlarmManager
import android.content.Context
import android.util.Log
import androidx.core.content.getSystemService
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmsManager {
    companion object {
        private const val TAG = "NextAlarm"
        private val ALLOW_LIST = listOf(
            "com.google.android.deskclock"
        )
    }

    fun fetchAlarms(context: Context): LocalTime? {
        var triggerTime: Long
        var localTime: LocalTime?
        var pendingIntent: String

        try {
            val alarmManager = context.getSystemService<AlarmManager>()!!
            val alarmClockInfo = alarmManager.nextAlarmClock

            if (alarmClockInfo != null) {
                pendingIntent = alarmClockInfo.showIntent?.creatorPackage ?: "UNKNOWN"
                triggerTime = alarmClockInfo.triggerTime

                if(pendingIntent in ALLOW_LIST){
                    Log.d(TAG, "Next alarm is scheduled by $pendingIntent with trigger time $triggerTime")
                    val instant = Instant.ofEpochMilli(triggerTime)
                    val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                    localTime = zonedDateTime.toLocalTime()

                    return localTime
                } else {
                    Log.d(TAG, "Alarm creator app is not in ALLOW_LIST, sending unavailable")
                }
            } else {
                Log.d(TAG, "No alarm is scheduled, sending unavailable")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting the next alarm info", e)
        }

        return null
    }
}