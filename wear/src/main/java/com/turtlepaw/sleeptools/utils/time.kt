package com.turtlepaw.sleeptools.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeParseException

data class TimeDifference(val hours: Long, val minutes: Long)
enum class AlarmType {
    SYSTEM_ALARM,
    USER_DEFINED
}

class TimeManager {
    fun calculateTimeDifference(targetTime: LocalTime, now: LocalTime = LocalTime.now()): TimeDifference {
        val currentDateTime = LocalTime.now()
        val duration = if (targetTime.isBefore(now)) {
            // If the target time is before the current time, it's on the next day
            Duration.between(currentDateTime, targetTime).plusDays(1)
        } else {
            Duration.between(currentDateTime, targetTime)
        }

        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes() + 1
        return if(minutes.toString() == "60")
            TimeDifference(hours + 1, 0)
        else TimeDifference(hours, minutes);
    }

    fun parseTime(time: String?, fallback: LocalTime?): LocalTime {
        return try {
            LocalTime.parse(time)
        } catch (e: DateTimeParseException) {
            fallback ?: LocalTime.NOON
        }
    }

    fun getWakeTime(
        useAlarm: Boolean,
        nextAlarm: LocalTime?,
        wakeTime: String?,
        fallback: LocalTime?
    ): Pair<LocalTime, AlarmType> {
        val parsedTime = parseTime(wakeTime, fallback);

        return if (useAlarm) {
            Log.d("TimeManager", "Next alarm: $nextAlarm")
            val alarmType = if (nextAlarm == null)
                AlarmType.USER_DEFINED else AlarmType.SYSTEM_ALARM
            Log.d("TimeManager", "Alarm type: $alarmType")
            Pair(nextAlarm ?: parsedTime, alarmType)
        } else {
            Pair(
                parsedTime,
                AlarmType.USER_DEFINED
            )
        }
    }

    fun getWakeTimeWithAlarm(
        context: Context,
        sharedPreferences: SharedPreferences
    ): Pair<LocalTime, AlarmType> {
        val alarmManager = AlarmsManager()
        val fallback = Settings.WAKE_TIME.getDefaultAsLocalTime()
        val useAlarm = sharedPreferences.getBoolean(Settings.ALARM.getKey(), Settings.ALARM.getDefaultAsBoolean())
        val nextAlarm = alarmManager.fetchAlarms(context)
        val wakeTime = sharedPreferences.getString(Settings.WAKE_TIME.getKey(), Settings.WAKE_TIME.getDefault())

        return this.getWakeTime(
            useAlarm,
            nextAlarm,
            wakeTime,
            fallback
        )
    }
}