/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.sunlight.presentation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.turtlepaw.sunlight.presentation.pages.GoalPicker
import com.turtlepaw.sunlight.presentation.pages.WearHome
import com.turtlepaw.sunlight.presentation.pages.history.WearHistory
import com.turtlepaw.sunlight.presentation.pages.settings.WearSettings
import com.turtlepaw.sunlight.presentation.theme.SleepTheme
import com.turtlepaw.sunlight.services.LightLoggerService
import com.turtlepaw.sunlight.utils.Settings
import com.turtlepaw.sunlight.utils.SettingsBasics
import com.turtlepaw.sunlight.utils.SunlightViewModel
import com.turtlepaw.sunlight.utils.SunlightViewModelFactory
import kotlinx.coroutines.delay
import java.time.LocalDate


enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    TIME_PICKER("/time-picker"),
    HISTORY("/history");

    fun getRoute(query: String? = null): String {
        return if(query != null){
            "$route/$query"
        } else route
    }
}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsBasics.HISTORY_STORAGE_BASE.getKey())

class MainActivity : ComponentActivity() {
    private lateinit var sunlightViewModelFactory: SunlightViewModelFactory
    private lateinit var sunlightViewModel: SunlightViewModel
    private val tag = "MainSleepActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )

        // Initialize your BedtimeViewModelFactory here
        sunlightViewModelFactory = SunlightViewModelFactory(dataStore)

        // Use the factory to create the BedtimeViewModel
        sunlightViewModel = ViewModelProvider(this, sunlightViewModelFactory)[SunlightViewModel::class.java]

        val scheduler = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(
            this,
            LightLoggerService::class.java
        )
        val scheduledIntent = PendingIntent.getService(
            this,
            0,
            intent,
            //PendingIntent.FLAG_UPDATE_CURRENT
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduler.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            60000, // 1m
            scheduledIntent
        )

        // Do an initial wake
        Log.d(tag, "Starting light listener")
        startService(intent)

        setContent {
            WearPages(sharedPreferences, sunlightViewModel, this)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "Refreshing database...")
        // Refresh the model
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )

        sunlightViewModelFactory = SunlightViewModelFactory(dataStore)

        sunlightViewModel = ViewModelProvider(this, sunlightViewModelFactory)[SunlightViewModel::class.java]
        Log.d(tag, "Database refreshed")

        setContent {
            WearPages(sharedPreferences, sunlightViewModel, this)
        }
    }
}

@Composable
fun WearPages(
    sharedPreferences: SharedPreferences,
    sunlightViewModel: SunlightViewModel,
    context: Context
){
    SleepTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        // Goal - the user's sun daily sun goal
        val goalInt = sharedPreferences.getInt(Settings.GOAL.getKey(), Settings.GOAL.getDefaultAsInt()) // Default to on
        var goal by remember { mutableStateOf(goalInt) }
        // Sunlight
        var sunlightHistory by remember { mutableStateOf<Set<Pair<LocalDate, Int>?>>(emptySet()) }
        var sunlightToday by remember { mutableStateOf<Int>(0) }
        var loading by remember { mutableStateOf(true) }
        // Suspended functions
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(key1 = sunlightViewModel) {
            sunlightHistory = sunlightViewModel.getAllHistory()
            sunlightToday = sunlightViewModel.getDay(LocalDate.now())?.second ?: 0
            loading = false
        }

        LaunchedEffect(key1 = sunlightViewModel) {
            val handler = Handler(Looper.getMainLooper())
            // Use a coroutine to run the code on the main thread
            while (true) {
                // Delay until the next minute
                delay(65_000 - (System.currentTimeMillis() % 65_000))

                // Update the current sunlight
                val today = sunlightViewModel.getDay(LocalDate.now())

                // Re-compose the composable
                handler.post {
                    if(today != null) sunlightToday = today.second
                }
            }
        }

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME.getRoute()
        ) {
            composable(Routes.HOME.getRoute()) {
                if(loading){
                    TimeText()
                    CircularProgressIndicator()
                } else {
                    WearHome(
                        navigate = { route ->
                            navController.navigate(route)
                        },
                        goal,
                        30
                    )
                }
            }
            composable(Routes.SETTINGS.getRoute()) {
                WearSettings(
                    navigate = { route ->
                        navController.navigate(route)
                    },
                    openGoalPicker = {
                      navController.navigate(Routes.TIME_PICKER.getRoute())
                    },
                    goal
                )
            }
            composable(Routes.TIME_PICKER.getRoute()){
                GoalPicker(
                    goal
                ) { value ->
                    goal = value
                    val editor = sharedPreferences.edit()
                    editor.putInt(Settings.GOAL.getKey(), value)
                    editor.apply()
                    navController.popBackStack()
                }
            }
            composable(Routes.HISTORY.getRoute()){
                WearHistory(
                    goal,
                    sunlightHistory,
                    loading
                )
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearHome(
        navigate = {},
        goal = Settings.GOAL.getDefaultAsInt(),
        today = 30
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    WearSettings(
        navigate = {},
        openGoalPicker = {},
        goal = Settings.GOAL.getDefaultAsInt()
    )
}