package com.turtlepaw.cats.mypet

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cat_status")

// Preferences Keys
private val HUNGER_KEY = intPreferencesKey("hunger")
private val TREATS_KEY = intPreferencesKey("treats")
private val HAPPINESS_KEY = intPreferencesKey("happiness")
private val HAPPINESS_REASONS_KEY = stringPreferencesKey("happiness_reasons")

suspend fun saveCatStatus(context: Context, status: CatStatus) {
    context.dataStore.edit { preferences ->
        preferences[HUNGER_KEY] = status.hunger
        preferences[TREATS_KEY] = status.treats
        preferences[HAPPINESS_KEY] = status.happiness
        // Convert happiness reasons map to a string for storage
        preferences[HAPPINESS_REASONS_KEY] = status.happinessReasons.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
}

fun getCatStatusFlow(context: Context): Flow<CatStatus> {
    return context.dataStore.data.map { preferences ->
        val hunger = preferences[HUNGER_KEY] ?: 0
        val treats = preferences[TREATS_KEY] ?: 0
        val happiness = preferences[HAPPINESS_KEY] ?: 0
        val happinessReasonsString = preferences[HAPPINESS_REASONS_KEY] ?: ""
        val happinessReasons = parseHappinessReasons(happinessReasonsString)
        CatStatus(hunger, treats, happiness, happinessReasons)
    }
}

fun parseHappinessReasons(happinessReasonsString: String): Map<String, Int> {
    // Return an empty map if the input string is empty
    if (happinessReasonsString.isBlank()) {
        return emptyMap()
    }

    return try {
        happinessReasonsString.split(",").mapNotNull {
            // Split each item by ':' and ensure it has exactly two parts
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
        // Log and handle exceptions if parsing fails
        Log.e("HappinessReasons", "Error parsing happiness reasons", e)
        emptyMap()
    }
}