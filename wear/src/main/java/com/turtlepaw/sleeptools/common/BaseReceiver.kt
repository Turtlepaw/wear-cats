package com.turtlepaw.sleeptools.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Keep
import com.turtlepaw.sleeptools.utils.BedtimeSensor
import com.turtlepaw.sleeptools.utils.Settings
import com.turtlepaw.sleeptools.utils.SettingsBasics
import java.time.LocalTime
import java.time.format.DateTimeParseException

@Keep
abstract class BaseReceiver: BroadcastReceiver() {
    protected abstract val tag: String
    protected abstract val sensorType: BedtimeSensor
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
    }

    private fun getSharedTime(sharedPreferences: SharedPreferences, key: String, fallback: LocalTime): LocalTime {
        val value = sharedPreferences.getString(key, null)
        return try {
            value?.let {
                LocalTime.parse(it)
            } ?: fallback
        } catch (e: DateTimeParseException) {
            fallback
        }
    }

    private fun isInTimeframe(sharedPreferences: SharedPreferences): Boolean {
        val useTimeframe = sharedPreferences.getBoolean(
            Settings.BEDTIME_TIMEFRAME.getKey(),
            Settings.BEDTIME_TIMEFRAME.getDefaultAsBoolean()
        )
        // The user doesn't want it to be in a timeframe
        if(!useTimeframe) return true
        // The user does want it to be in a certain timeframe
        // let's get those times
        val timeframeStart = getSharedTime(
            sharedPreferences,
            Settings.BEDTIME_START.getKey(),
            Settings.BEDTIME_START.getDefaultAsLocalTime()
        )
        val timeframeEnd = getSharedTime(
            sharedPreferences,
            Settings.BEDTIME_END.getKey(),
            Settings.BEDTIME_END.getDefaultAsLocalTime()
        )
        val now = LocalTime.now()

        // Let's check if it's between the start and end, considering crossing midnight
        return if (timeframeStart.isBefore(timeframeEnd)) {
            // Timeframe is within the same day
            now.isAfter(timeframeStart) && now.isBefore(timeframeEnd)
        } else {
            // Timeframe crosses midnight
            now.isAfter(timeframeStart) || now.isBefore(timeframeEnd)
        }
    }

    private fun isEnabled(sharedPreferences: SharedPreferences): Boolean {
        val bedtimeStringSensor = sharedPreferences.getString(
            Settings.BEDTIME_SENSOR.getKey(),
            Settings.BEDTIME_SENSOR.getDefault()
        )

        return bedtimeStringSensor == sensorType.toString()
    }

    fun checkShouldRun(context: Context, intent: Intent): Boolean {
        Log.d(tag, "Received intent: ${intent.action}")
        val sharedPreferences = getSharedPreferences(context)
        val isEnabled = this.isEnabled(sharedPreferences)
        val isInTimeframe = this.isInTimeframe(sharedPreferences)

        return if(!isEnabled){
            Log.d(tag, "Sensor not enabled, not updating sensor")
            false
        } else if(!isInTimeframe) {
            Log.d(tag, "Not in timeframe, not updating sensor")
            false
        } else {
            Log.d(tag, "Updating sensor")
            true
        }
    }
}