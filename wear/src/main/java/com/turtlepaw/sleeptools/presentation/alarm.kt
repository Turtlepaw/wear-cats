package com.turtlepaw.sleeptools.presentation

/*
    This file includes code derived from the home-assistant/android (https://github.com/home-assistant/android/blob/master/LICENSE.md)
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
    }

    fun fetchAlarms(context: Context): LocalTime? {
        var triggerTime = 0L
        var localTime: LocalTime? = null
        var pendingIntent = ""

        try {
            val alarmManager = context.getSystemService<AlarmManager>()!!
            val alarmClockInfo = alarmManager.nextAlarmClock

            if (alarmClockInfo != null) {
                pendingIntent = alarmClockInfo.showIntent?.creatorPackage ?: "UNKNOWN"
                triggerTime = alarmClockInfo.triggerTime

                Log.d(TAG, "Next alarm is scheduled by $pendingIntent with trigger time $triggerTime")

                val instant = Instant.ofEpochMilli(triggerTime)
                val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                localTime = zonedDateTime.toLocalTime()

                return localTime
            } else {
                Log.d(TAG, "No alarm is scheduled, sending unavailable")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting the next alarm info", e)
        }

        return null
    }
}