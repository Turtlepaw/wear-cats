package com.turtlepaw.sleeptools.utils

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.turtlepaw.sleeptools.utils.BedtimeViewModel.PreferencesKeys.BEDTIME_HISTORY_KEY
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class BedtimeViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
    private object PreferencesKeys {
        val BEDTIME_HISTORY_KEY = stringSetPreferencesKey("bedtime_history")
    }

    suspend fun save(date: LocalDateTime, type: BedtimeSensor) {
        dataStore.edit { preferences ->
            val bedtimeHistory = (preferences[BEDTIME_HISTORY_KEY] ?: mutableSetOf()).toMutableSet()
            bedtimeHistory.add("$date - $type")
            preferences[BEDTIME_HISTORY_KEY] = bedtimeHistory
        }
    }

    suspend fun delete(date: LocalDateTime) {
        dataStore.edit { preferences ->
            val bedtimeHistory = (preferences[BEDTIME_HISTORY_KEY] ?: mutableSetOf()).toMutableSet()
            Log.d("Cleaner", "Found ${bedtimeHistory.filter { it.startsWith(date.toString()) }} ($bedtimeHistory | ${date.toString()})")
            bedtimeHistory.removeIf { it.startsWith(date.toString()) } // Remove entries with matching date string
            preferences[BEDTIME_HISTORY_KEY] = bedtimeHistory
        }
    }


    suspend fun deleteAll() {
        dataStore.edit { preferences ->
            preferences[BEDTIME_HISTORY_KEY] = mutableSetOf<String>().toMutableSet()
        }
    }


    private fun parseDate(str: String): Pair<LocalDateTime, BedtimeSensor>? {
        Log.d("Parser", "Parsing $str")
        val split = str.split(" - ")
        if (split.size != 2) {
            return null
        }
        val dateStr = split[0]
        val sensorTypeStr = split[1]
        Log.d("Parser", "Parsed $dateStr as date and $sensorTypeStr as sensor")
        val date = try {
            LocalDateTime.parse(dateStr)
        } catch (e: DateTimeParseException) {
            return null
        }
        val sensorType = try {
            BedtimeSensor.valueOf(sensorTypeStr)
        } catch (e: IllegalArgumentException) {
            return null
        }
        return date to sensorType
    }

    suspend fun getHistory(): Set<Pair<LocalDateTime, BedtimeSensor>?> {
        val preferences = dataStore.data.first() // blocking call to get the latest preferences
        return preferences[BEDTIME_HISTORY_KEY]?.mapNotNull { entry ->
            val split = entry.split(" - ")
            if (split.size == 2) {
                parseDate(entry) // Extract and parse date string only
            } else {
                null
            }
        }?.toSet() ?: emptySet()
    }

    suspend fun getLatest(): LocalDateTime? {
        val preferences = dataStore.data.first() // blocking call to get the latest preferences
        val bedtimeHistory = preferences[BEDTIME_HISTORY_KEY] ?: emptySet()
        return bedtimeHistory.mapNotNull { entry ->
            val split = entry.split(" - ")
            if (split.size == 2) {
                parseDate(entry)?.first // Extract and parse date string only
            } else {
                null
            }
        }.lastOrNull()
    }


    suspend fun getItem(key: String): LocalDateTime? {
        val preferences = dataStore.data.first() // blocking call to get the latest preferences
        val bedtimeHistory = preferences[BEDTIME_HISTORY_KEY] ?: emptySet()
        return bedtimeHistory.find { entry ->
            val split = entry.split(" - ")
            split.size == 2 && split[0] == key
        }?.let { parseDate(it)?.first }
    }
}

class BedtimeViewModelFactory(private val dataStore: DataStore<Preferences>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BedtimeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BedtimeViewModel(dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
