package com.turtlepaw.cats.mypet

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

// Define the DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cat_status")

// Preferences Keys
private val HUNGER_KEY = intPreferencesKey("hunger")
private val TREATS_KEY = intPreferencesKey("treats")
private val HAPPINESS_KEY = intPreferencesKey("happiness")
private val DAILY_TREATS_AVAILABLE_KEY = intPreferencesKey("daily_treats_available") // Renamed from maxTreats
private val HAPPINESS_REASONS_KEY = stringPreferencesKey("happiness_reasons")
private val LAST_FED_KEY = stringPreferencesKey("last_fed")
private val LAST_UPDATE_KEY = stringPreferencesKey("last_update") // Add a key for last update

suspend fun saveCatStatus(context: Context, status: CatStatus) {
    context.dataStore.edit { preferences ->
        preferences[HUNGER_KEY] = status.hunger
        preferences[TREATS_KEY] = status.treats
        preferences[HAPPINESS_KEY] = status.happiness
        preferences[DAILY_TREATS_AVAILABLE_KEY] = status.dailyTreatsAvailable // Updated key
        // Convert happiness reasons map to a string for storage
        preferences[HAPPINESS_REASONS_KEY] = status.happinessReasons.entries.joinToString(",") { "${it.key}:${it.value}" }
        preferences[LAST_FED_KEY] = status.lastFed?.toString() ?: ""
        preferences[LAST_UPDATE_KEY] = status.lastUpdate?.toString() ?: "" // Store last update date
    }
}

fun getCatStatusFlow(context: Context): Flow<CatStatus> {
    return context.dataStore.data.map { preferences ->
        val hunger = preferences[HUNGER_KEY] ?: 0
        val treats = preferences[TREATS_KEY] ?: 0
        val dailyTreatsAvailable = preferences[DAILY_TREATS_AVAILABLE_KEY] ?: 0
        val happiness = preferences[HAPPINESS_KEY] ?: 0
        val happinessReasonsString = preferences[HAPPINESS_REASONS_KEY] ?: ""
        val happinessReasons = parseHappinessReasons(happinessReasonsString)
        val lastFedString = preferences[LAST_FED_KEY] ?: ""
        val lastFed = try {
            if (lastFedString.isNotBlank()) LocalDateTime.parse(lastFedString) else null
        } catch (e: Exception) {
            Log.e("CatStatus", "Error parsing lastFed", e)
            null
        }
        val lastUpdateString = preferences[LAST_UPDATE_KEY] ?: ""
        val lastUpdate = try {
            if (lastUpdateString.isNotBlank()) LocalDateTime.parse(lastUpdateString) else null
        } catch (e: Exception) {
            Log.e("CatStatus", "Error parsing lastUpdate", e)
            null
        }

        CatStatus(hunger, treats, dailyTreatsAvailable, happiness, happinessReasons, lastFed, lastUpdate)
    }
}

fun parseHappinessReasons(happinessReasonsString: String): Map<String, Int> {
    if (happinessReasonsString.isBlank()) {
        return emptyMap()
    }

    return try {
        happinessReasonsString.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim().toIntOrNull() ?: return@mapNotNull null
                key to value
            } else {
                null
            }
        }.toMap()
    } catch (e: Exception) {
        Log.e("HappinessReasons", "Error parsing happiness reasons", e)
        emptyMap()
    }
}
