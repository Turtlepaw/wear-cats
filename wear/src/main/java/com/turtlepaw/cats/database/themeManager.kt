package com.turtlepaw.cats.database

import android.app.Application
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.Colors
import com.turtlepaw.cats.presentation.theme.celestialCalm
import com.turtlepaw.cats.presentation.theme.coastalSunriseColors
import com.turtlepaw.cats.presentation.theme.defaultColors
import com.turtlepaw.cats.presentation.theme.forestSunriseColors
import com.turtlepaw.cats.presentation.theme.greenColors
import com.turtlepaw.cats.presentation.theme.verdantTerraColors
import com.turtlepaw.cats.presentation.theme.violetColors
import com.turtlepaw.cats.presentation.theme.yellowColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

val colorsMap = mapOf(
    1 to defaultColors,
    2 to greenColors,
    3 to violetColors,
    4 to yellowColors
)

val colorsNames = mapOf(
    1 to "Default",
    2 to "Green",
    3 to "Violet",
    4 to "Yellow",
    5 to "Coastal Sunrise",
    6 to "Mossy Canyon",
    7 to "Celestial Calm"
)

class ThemeViewModel(val sharedPreferences: SharedPreferences) : ViewModel() {
    private val _currentTheme = MutableStateFlow(loadThemePreference())
    val currentTheme: StateFlow<Colors> = _currentTheme

    fun setTheme(theme: Colors) {
        _currentTheme.value = theme
        saveThemePreference(_currentTheme.value)
    }

    private fun saveThemePreference(theme: Colors) {
        sharedPreferences.edit().putInt("theme", getThemeId(theme)).apply()
    }

    private fun loadThemePreference(): Colors {
        return getColorsFromId(
            sharedPreferences.getInt("theme", 1)
        )
    }

    private fun getThemeId(colors: Colors): Int {
        return colorsMap.entries.find { it.value == colors }?.key ?: 1
    }

    private fun getColorsFromId(id: Int): Colors {
        return colorsMap.entries.find { it.key == id }?.value ?: defaultColors
    }
}


class ThemeViewModelFactory(private val sharedPreferences: SharedPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
