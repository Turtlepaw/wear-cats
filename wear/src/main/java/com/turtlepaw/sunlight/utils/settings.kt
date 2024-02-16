package com.turtlepaw.sunlight.utils

import android.content.Context
import java.time.LocalTime

enum class Settings(private val key: String, private val default: Any?) {
    GOAL("goal", 15),
    SUN_THRESHOLD("threshold", 5000),
    TIMEOUT("timeout", LocalTime.of(20, 0)),
    WAKEUP("wakeup", LocalTime.of(5, 0));

    fun getKey(): String {
        return key
    }

    fun getDefault(): String {
        return when (default) {
            is String -> {
                default
            }

            else -> {
                default.toString()
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

    fun getDefaultAsInt(): Int {
        return when (default) {
            is Int -> {
                default
            }

            is String -> {
                default.toInt()
            }

            else -> {
                0
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