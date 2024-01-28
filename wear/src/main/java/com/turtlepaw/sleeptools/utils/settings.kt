package com.turtlepaw.sleeptools.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class Settings(private val key: String, private val default: Any?) {
    WAKE_TIME("wake_time", LocalTime.of(10, 30)),
    ALARM("use_system_alarm", true),
    ALERTS("use_notifications", false),
    STORAGE_BASE("bedtime_history", null);

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