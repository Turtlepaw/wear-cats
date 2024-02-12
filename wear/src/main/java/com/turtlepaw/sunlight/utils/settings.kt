package com.turtlepaw.sunlight.utils

import android.content.Context
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class Settings(private val key: String, private val default: Any?) {
    GOAL("goal", 15);

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