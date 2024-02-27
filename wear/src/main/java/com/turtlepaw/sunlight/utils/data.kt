package com.turtlepaw.sunlight.utils

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.turtlepaw.sunlight.utils.SunlightViewModel.PreferencesKeys.SUNLIGHT_HISTORY_KEY
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeParseException

class SunlightViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
    private object PreferencesKeys {
        val SUNLIGHT_HISTORY_KEY = stringSetPreferencesKey("sunlight")
    }

    /**
     * Creates a new entry in the sunlight history
     * that "starts" a new day
     */
    suspend fun startDay() {
        dataStore.edit { preferences ->
            val history = (preferences[SUNLIGHT_HISTORY_KEY] ?: mutableSetOf()).toMutableSet()
            val date = LocalDate.now()
            history.add("$date - 0")
            preferences[SUNLIGHT_HISTORY_KEY] = history
        }
    }

    suspend fun addMinute(date: LocalDate){
        val day = getDay(date)
        val minutes = (day?.second ?: 0).plus(1)
        if(day == null) startDay()
        dataStore.edit { preferences ->
            val history = (preferences[SUNLIGHT_HISTORY_KEY] ?: mutableSetOf()).toMutableSet()
            Log.d("DataStore", "Minutes: ${day?.second} $minutes ($date - ${history.find { it.startsWith(date.toString()) }})")
            history.removeIf { it.startsWith(date.toString()) }
            history.add("$date - $minutes")
            preferences[SUNLIGHT_HISTORY_KEY] = history
        }
    }

    suspend fun add(date: LocalDate, time: Int){
        val day = getDay(date)
        val minutes = (day?.second ?: 0).plus(time)
        if(day == null) startDay()
        dataStore.edit { preferences ->
            val history = (preferences[SUNLIGHT_HISTORY_KEY] ?: mutableSetOf()).toMutableSet()
            history.removeIf { it.startsWith(date.toString()) }
            history.add("$date - $minutes")
            preferences[SUNLIGHT_HISTORY_KEY] = history
        }
    }

    private fun parseEntry(str: String): Pair<LocalDate, Int>? {
        val split = str.split(" - ")
        if (split.size != 2) {
            return null
        }
        val dateStr = split[0]
        val minutesStr = split[1]
        val date = try {
            LocalDate.parse(dateStr)
        } catch (e: DateTimeParseException) {
            return null
        }
        val minutes = try {
            minutesStr.toInt()
        } catch (e: IllegalArgumentException) {
            return null
        }
        return date to minutes
    }

    suspend fun getAllHistory(): Set<Pair<LocalDate, Int>?> {
        val preferences = dataStore.data.first() // blocking call to get the latest preferences
        return preferences[SUNLIGHT_HISTORY_KEY]?.mapNotNull { entry ->
            val split = entry.split(" - ")
            if (split.size == 2) {
                parseEntry(entry) // Extract and parse date string only
            } else {
                null
            }
        }?.toSet() ?: emptySet()
    }

    suspend fun getDay(date: LocalDate): Pair<LocalDate, Int>? {
        val preferences = dataStore.data.first() // blocking call to get the latest preferences
        val bedtimeHistory = preferences[SUNLIGHT_HISTORY_KEY] ?: emptySet()
        val result = bedtimeHistory.find { it.startsWith(date.toString()) }

        return if(result == null) null
        else parseEntry(result)
    }
}

class SunlightViewModelFactory(private val dataStore: DataStore<Preferences>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SunlightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SunlightViewModel(dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
