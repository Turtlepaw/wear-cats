package com.turtlepaw.sleeptools.utils

import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeParseException

data class TimeDifference(val hours: Long, val minutes: Long)

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
        return TimeDifference(hours, minutes)
    }

    fun getWakeTime(useAlarm: Boolean, nextAlarm: LocalTime?, wakeTime: String?): LocalTime {
        val parsedTime = try {
            LocalTime.parse(wakeTime)
        } catch (e: DateTimeParseException) {
            // Handle parsing error, use a default value, or show an error message
            LocalTime.NOON
        }

        return if (useAlarm) {
            nextAlarm ?: parsedTime
        } else {
            parsedTime ?: LocalTime.of(8, 30)
        }
    }
}