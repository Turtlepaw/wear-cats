package com.turtlepaw.sleeptools.utils

import android.content.Context
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class Settings(private val key: String, private val default: Any?) {
    WAKE_TIME("wake_time", LocalTime.of(10, 30)),
    ALARM("use_system_alarm", true),
    ALERTS("use_notifications", false),
    HISTORY_STORAGE_BASE("bedtime_history", null),
    SHARED_PREFERENCES("SleepToolsSettings", Context.MODE_PRIVATE);

    fun getKey(): String {
        return key
    }

    fun getDefault(): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm")
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