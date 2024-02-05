package com.turtlepaw.sleeptools.utils

import android.content.Context
import android.util.Log
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class BedtimeSensor {
    BEDTIME,
    CHARGING
}

enum class Settings(private val key: String, private val default: Any?) {
    WAKE_TIME("wake_time", LocalTime.of(10, 30)),
    ALARM("use_system_alarm", true),
    ALERTS("use_notifications", false),
    BEDTIME_SENSOR("bedtime_sensor", BedtimeSensor.BEDTIME),
    BEDTIME_ENABLED("bedtime_enabled", true),
    BEDTIME_TIMEFRAME("bedtime_timeframe", true),
    BEDTIME_START("bedtime_start", LocalTime.of(20, 0)),
    BEDTIME_END("bedtime_end", LocalTime.of(8, 0));

    fun getKey(): String {
        return key
    }

    fun getDefault(): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        return when (default) {
            is String -> {
                default
            }

            is LocalTime -> {
                formatter.format(default)
            }

            else -> {
                default.toString()
            }
        }
    }

    fun getDefaultAsBoolean(): Boolean {
        return when (default) {
            is Boolean -> {
                default
            }

            is String -> {
                default.toBoolean()
            }

            else -> {
                true
            }
        }
    }

    fun getDefaultAsLocalTime(): LocalTime {
        return when (default) {
            is LocalTime -> {
                default
            }

            is String -> {
                LocalTime.parse(default)
            }

            else -> {
                LocalTime.of(10, 30)
            }
        }
    }
}

enum class SettingsBasics(private val key: String, private val mode: Int?) {
    HISTORY_STORAGE_BASE("bedtime_history", null),
    SHARED_PREFERENCES("SleepToolsSettings", Context.MODE_PRIVATE);

    fun getKey(): String {
        return key
    }

    fun getMode(): Int {
        return mode ?: Context.MODE_PRIVATE
    }
}

fun verifySensor(context: Context, sensor: BedtimeSensor): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        SettingsBasics.SHARED_PREFERENCES.getKey(),
        SettingsBasics.SHARED_PREFERENCES.getMode()
    )

    val timeManager = TimeManager()
    val bedtimeStringSensor = sharedPreferences.getString(Settings.BEDTIME_SENSOR.getKey(), Settings.BEDTIME_SENSOR.getDefault())
    val bedtimeSensor = if(bedtimeStringSensor == "BEDTIME") BedtimeSensor.BEDTIME else BedtimeSensor.CHARGING;
    Log.d("VerifySensor", "Sensor set to $bedtimeStringSensor (requires $sensor, result: ${bedtimeStringSensor != sensor.toString()})")
    if(bedtimeStringSensor != sensor.toString()) return false

    val useTimeframe = sharedPreferences.getBoolean(Settings.BEDTIME_TIMEFRAME.getKey(), Settings.BEDTIME_TIMEFRAME.getDefaultAsBoolean())
    if(!useTimeframe) return true
    val timeframeStartString = sharedPreferences.getString(Settings.BEDTIME_START.getKey(), Settings.BEDTIME_START.getDefault())
    val timeframeEndString = sharedPreferences.getString(Settings.BEDTIME_END.getKey(), Settings.BEDTIME_END.getDefault())
    val timeframeStart = timeManager.parseTime(timeframeStartString, Settings.BEDTIME_START.getDefaultAsLocalTime())
    val timeframeEnd = timeManager.parseTime(timeframeEndString, Settings.BEDTIME_END.getDefaultAsLocalTime())
    val now = LocalTime.now()

    return now.isAfter(timeframeStart) && now.isBefore(timeframeEnd)
}