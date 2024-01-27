package com.turtlepaw.sleeptools.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import com.turtlepaw.sleeptools.utils.BedtimeViewModel.PreferencesKeys.BEDTIME_HISTORY_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

private const val USER_PREFERENCES_NAME = "bedtime_key"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)
class BedtimeViewModel(private val context: Context) : ViewModel() {
    private object PreferencesKeys {
        val BEDTIME_HISTORY_KEY = stringSetPreferencesKey("bedtime_history")
    }

    suspend fun save(date: LocalDateTime) {
        context.dataStore.edit { preferences ->
            var bedtimeHistory = preferences[BEDTIME_HISTORY_KEY] ?: mutableSetOf()
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
        val preferences = context.dataStore.data.first() // blocking call to get the latest preferences
        return preferences[BEDTIME_HISTORY_KEY]?.map { date ->
            this.parseDate(date)
        }?.toSet() ?: emptySet()
    }

    suspend fun getLatest(): LocalDateTime? {
        val preferences = context.dataStore.data.first() // blocking call to get the latest preferences
        val bedtimeHistory = preferences[BEDTIME_HISTORY_KEY] ?: emptySet()
        return bedtimeHistory.lastOrNull()?.let { parseDate(it) }
    }
}