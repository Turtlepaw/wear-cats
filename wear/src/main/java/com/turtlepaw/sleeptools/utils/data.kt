package com.turtlepaw.sleeptools.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.turtlepaw.sleeptools.utils.BedtimeViewModel.PreferencesKeys.BEDTIME_HISTORY_KEY
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

const val BEDTIME_STORAGE_KEY = "bedtime_key"
class BedtimeViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
    private object PreferencesKeys {
        val BEDTIME_HISTORY_KEY = stringSetPreferencesKey("bedtime_history")
    }

    suspend fun save(date: LocalDateTime) {
        dataStore.edit { preferences ->
            val bedtimeHistory = (preferences[BEDTIME_HISTORY_KEY] ?: mutableSetOf()).toMutableSet()
            bedtimeHistory += date.toString()
            preferences[BEDTIME_HISTORY_KEY] = bedtimeHistory
        }
    }


    private fun parseDate(str: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(str)
        } catch(e: DateTimeParseException) {
            null
        }
    }
    suspend fun getHistory(): Set<LocalDateTime?> {
        val preferences = dataStore.data.first() // blocking call to get the latest preferences
        return preferences[BEDTIME_HISTORY_KEY]?.map { date ->
            this.parseDate(date)
        }?.toSet() ?: emptySet()
    }

    suspend fun getLatest(): LocalDateTime? {
        val preferences = dataStore.data.first() // blocking call to get the latest preferences
        val bedtimeHistory = preferences[BEDTIME_HISTORY_KEY] ?: emptySet()
        return bedtimeHistory.lastOrNull()?.let { parseDate(it) }
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
