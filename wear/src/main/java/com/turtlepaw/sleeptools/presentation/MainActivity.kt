/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.sleeptools.presentation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.turtlepaw.sleeptools.presentation.pages.WakeTimePicker
import com.turtlepaw.sleeptools.presentation.pages.history.WearHistory
import com.turtlepaw.sleeptools.presentation.pages.WearHome
import com.turtlepaw.sleeptools.presentation.pages.WearSettings
import com.turtlepaw.sleeptools.presentation.pages.history.WearHistoryDelete
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.turtlepaw.sleeptools.services.BedtimeModeService
import com.turtlepaw.sleeptools.utils.AlarmType
import com.turtlepaw.sleeptools.utils.AlarmsManager
import com.turtlepaw.sleeptools.utils.BedtimeModeManager
import com.turtlepaw.sleeptools.utils.BedtimeViewModel
import com.turtlepaw.sleeptools.utils.BedtimeViewModelFactory
import com.turtlepaw.sleeptools.utils.Settings
import com.turtlepaw.sleeptools.utils.SettingsBasics
import com.turtlepaw.sleeptools.utils.TimeManager
import java.time.LocalDateTime
import java.time.LocalTime

enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    TIME_PICKER("/time-picker"),
    HISTORY("/history"),
    DELETE_HISTORY("/history/delete");

    fun getRoute(query: String? = null): String {
        return if(query != null){
            "$route/$query"
        } else route
    }
}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsBasics.HISTORY_STORAGE_BASE.getKey())

class MainActivity : ComponentActivity() {
    private lateinit var bedtimeViewModelFactory: BedtimeViewModelFactory
    private lateinit var bedtimeViewModel: BedtimeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        // Start listening for bedtime mode
        startService(
            Intent(
                this,
                BedtimeModeService::class.java
            )
        )
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )

        // Initialize your BedtimeViewModelFactory here
        bedtimeViewModelFactory = BedtimeViewModelFactory(dataStore)

        // Use the factory to create the BedtimeViewModel
        bedtimeViewModel = ViewModelProvider(this, bedtimeViewModelFactory)[BedtimeViewModel::class.java]

        setContent {
            WearPages(sharedPreferences, bedtimeViewModel, this)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MAIN_ACTIVITY", "Refreshing database...")
        // You might not need to recreate the ViewModel here, as you've already created it in onCreate
        // bedtimeViewModel = BedtimeViewModel(this)
        Log.d("MAIN_ACTIVITY", "Database refreshed")
    }
}

@Composable
fun WearPages(sharedPreferences: SharedPreferences, bedtimeViewModel: BedtimeViewModel, context: Context){
    SleepTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        // Creates a new alarm & time manager
        val timeManager = TimeManager()
        val alarmManager = AlarmsManager()
        // Fetches the wake time from settings
        val wakeTimeString = sharedPreferences.getString(Settings.WAKE_TIME.getKey(), Settings.WAKE_TIME.getDefault()) // Default to midnight
        // Use Alarm - uses system alarm as wake time
        val useAlarmBool = sharedPreferences.getBoolean(Settings.ALARM.getKey(), Settings.ALARM.getDefaultAsBoolean()) // Default to on
        var useAlarm by remember { mutableStateOf(useAlarmBool) }
        // Use Alerts - sends alerts when to go to bed
        val useAlertsBool = sharedPreferences.getBoolean(Settings.ALERTS.getKey(), Settings.ALERTS.getDefaultAsBoolean()) // Default to on
        var useAlerts by remember { mutableStateOf(useAlertsBool) }
        // Fetches the next alarm from android's alarm manager
        val nextAlarm = alarmManager.fetchAlarms(context)
        // Uses Settings.Globals to get bedtime mode
        val bedtimeModeManager = BedtimeModeManager()
        var lastBedtime by remember { mutableStateOf<LocalDateTime?>(null) }
        // History
        var history by remember { mutableStateOf<Set<LocalDateTime?>>(emptySet()) }
        var loading by remember { mutableStateOf<Boolean>(true) }
        LaunchedEffect(key1 = Unit) {
            history = bedtimeViewModel.getHistory()
            loading = false
        }
        LaunchedEffect(key1 = Unit) {
            // Launch a coroutine to perform async operations
            lastBedtime = bedtimeModeManager.getLastBedtime(bedtimeViewModel)
        }
        // Parses the wake time and decides if it should use
        // user defined or system defined
        var wakeTime = timeManager.getWakeTime(
            useAlarm,
            nextAlarm,
            wakeTimeString,
            Settings.WAKE_TIME.getDefaultAsLocalTime()
        )
        var userWakeTime = timeManager.parseTime(wakeTimeString, Settings.WAKE_TIME.getDefaultAsLocalTime())

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME.getRoute()
        ) {
            composable(Routes.HOME.getRoute()) {
                WearHome(
                    navigate = { route ->
                        navController.navigate(route)
                    },
                    wakeTime,
                    nextAlarm = nextAlarm ?: wakeTime.first,
                    timeManager,
                    lastBedtime
                )
            }
            composable(Routes.SETTINGS.getRoute()) {
                WearSettings(
                    navigate = { route ->
                        navController.navigate(route)
                    },
                    openWakeTimePicker = {
                      navController.navigate(Routes.TIME_PICKER.getRoute())
                    },
                    wakeTime,
                    userWakeTime,
                    setAlarm = { value ->
                        useAlarm = value
                        val editor = sharedPreferences.edit()
                        editor.putBoolean(Settings.ALARM.getKey(), value)
                        editor.apply()
                    },
                    useAlarm,
                    setAlerts = { value ->
                        useAlerts = value
                        val editor = sharedPreferences.edit()
                        editor.putBoolean(Settings.ALERTS.getKey(), value)
                        editor.apply()
                    },
                    useAlerts
                )
            }
            composable(Routes.TIME_PICKER.getRoute()){
                WakeTimePicker(
                    closePicker = {
                        navController.popBackStack()
                    },
                    userWakeTime,
                    setWakeTime = { value ->
                        // Set the wake time ONLY if
                        // it's user defined
                        if(wakeTime.second === AlarmType.USER_DEFINED)
                            wakeTime = Pair(value, AlarmType.USER_DEFINED)

                        userWakeTime = value
                        val editor = sharedPreferences.edit()
                        editor.putString("wake_time", value.toString())
                        editor.apply()
                    }
                )
            }
            composable(Routes.HISTORY.getRoute()){
                WearHistory(
                    navController,
                    history,
                    loading
                )
            }
            composable(Routes.DELETE_HISTORY.getRoute("{id}")) {
                WearHistoryDelete(
                    bedtimeViewModel,
                    timeManager.parseDateTime(it.arguments?.getString("id")!!),
                    navigation = navController,
                    onDelete = { time ->
                        val mutated = history.toMutableSet()
                        mutated.remove(time)
                        history = mutated
                    }
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
        wakeTime = Pair(
            LocalTime.of(10, 30),
            AlarmType.SYSTEM_ALARM
        ),
        nextAlarm = LocalTime.of(7, 30),
        timeManager = TimeManager(),
        null
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    WearSettings(
        navigate = {},
        openWakeTimePicker = {},
        wakeTime = Pair(
            Settings.WAKE_TIME.getDefaultAsLocalTime(),
            AlarmType.SYSTEM_ALARM
        ),
        userWakeTime = Settings.WAKE_TIME.getDefaultAsLocalTime(),
        setAlarm = {},
        useAlarm = Settings.ALARM.getDefaultAsBoolean(),
        setAlerts = {},
        alerts = Settings.ALERTS.getDefaultAsBoolean()
    )
}