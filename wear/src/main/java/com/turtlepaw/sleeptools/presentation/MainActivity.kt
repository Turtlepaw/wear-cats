/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.sleeptools.presentation

import android.content.Context
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.turtlepaw.sleeptools.presentation.pages.TimePicker
import com.turtlepaw.sleeptools.presentation.pages.WearHome
import com.turtlepaw.sleeptools.presentation.pages.history.Item
import com.turtlepaw.sleeptools.presentation.pages.history.WearHistory
import com.turtlepaw.sleeptools.presentation.pages.history.WearHistoryDelete
import com.turtlepaw.sleeptools.presentation.pages.settings.WearBedtimeSensorSetting
import com.turtlepaw.sleeptools.presentation.pages.settings.WearBedtimeSettings
import com.turtlepaw.sleeptools.presentation.pages.settings.WearSettings
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.turtlepaw.sleeptools.utils.AlarmType
import com.turtlepaw.sleeptools.utils.AlarmsManager
import com.turtlepaw.sleeptools.utils.BedtimeSensor
import com.turtlepaw.sleeptools.utils.BedtimeViewModel
import com.turtlepaw.sleeptools.utils.BedtimeViewModelFactory
import com.turtlepaw.sleeptools.utils.Settings
import com.turtlepaw.sleeptools.utils.SettingsBasics
import com.turtlepaw.sleeptools.utils.TimeManager
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    SETTINGS_BEDTIME("/settings/bedtime"),
    SETTINGS_TIMEFRAME_START("/settings/bedtime/start"),
    SETTINGS_TIMEFRAME_END("/settings/bedtime/end"),
    SETTINGS_BEDTIME_SENSOR("/settings/bedtime/sensor"),
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
        bedtimeViewModelFactory = BedtimeViewModelFactory(dataStore)

        // Use the factory to create the BedtimeViewModel
        bedtimeViewModel = ViewModelProvider(this, bedtimeViewModelFactory)[BedtimeViewModel::class.java]

        setContent {
            WearPages(sharedPreferences, bedtimeViewModel, this)
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

        bedtimeViewModelFactory = BedtimeViewModelFactory(dataStore)

        bedtimeViewModel = ViewModelProvider(this, bedtimeViewModelFactory)[BedtimeViewModel::class.java]
        Log.d(tag, "Database refreshed")

        setContent {
            WearPages(sharedPreferences, bedtimeViewModel, this)
        }
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
        // Settings timeframe start
        // Fetches the wake time from settings
        val timeframeStartString = sharedPreferences.getString(Settings.BEDTIME_START.getKey(), Settings.BEDTIME_START.getDefault())
        val timeframeEndString = sharedPreferences.getString(Settings.BEDTIME_END.getKey(), Settings.BEDTIME_END.getDefault())
        var useTimeframe = sharedPreferences.getBoolean(Settings.BEDTIME_TIMEFRAME.getKey(), Settings.BEDTIME_TIMEFRAME.getDefaultAsBoolean())
        val bedtimeStringSensor = sharedPreferences.getString(Settings.BEDTIME_SENSOR.getKey(), Settings.BEDTIME_SENSOR.getDefault())
        var bedtimeSensor = if(bedtimeStringSensor == "BEDTIME") BedtimeSensor.BEDTIME else BedtimeSensor.CHARGING
        // parsed
        var timeframeStart = timeManager.parseTime(timeframeStartString, Settings.BEDTIME_START.getDefaultAsLocalTime())
        var timeframeEnd = timeManager.parseTime(timeframeEndString, Settings.BEDTIME_END.getDefaultAsLocalTime())
        // Use Alarm - uses system alarm as wake time
        val useAlarmBool = sharedPreferences.getBoolean(Settings.ALARM.getKey(), Settings.ALARM.getDefaultAsBoolean()) // Default to on
        var useAlarm by remember { mutableStateOf(useAlarmBool) }
        // Use Alerts - sends alerts when to go to bed
        val useAlertsBool = sharedPreferences.getBoolean(Settings.ALERTS.getKey(), Settings.ALERTS.getDefaultAsBoolean()) // Default to on
        var useAlerts by remember { mutableStateOf(useAlertsBool) }
        // Fetches the next alarm from android's alarm manager
        val nextAlarm = alarmManager.fetchAlarms(context)
        // Uses Settings.Globals to get bedtime mode
        var lastBedtime by remember { mutableStateOf<LocalDateTime?>(null) }
        // History
        var history by remember { mutableStateOf<Set<Pair<LocalDateTime, BedtimeSensor>?>>(emptySet()) }
        var loading by remember { mutableStateOf(true) }
        // Suspended functions
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(key1 = bedtimeViewModel) {
            history = bedtimeViewModel.getHistory()
            loading = false
        }
        LaunchedEffect(key1 = bedtimeViewModel) {
            // Launch a coroutine to perform async operations
            lastBedtime = bedtimeViewModel.getLatest()
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
            composable(Routes.SETTINGS_BEDTIME.getRoute()) {
                WearBedtimeSettings(
                    navigator = navController,
                    timeframeStart,
                    setTimeStart = { value ->
                        timeframeStart = value
                        val editor = sharedPreferences.edit()
                        editor.putString(Settings.BEDTIME_START.getKey(), value.toString())
                        editor.apply()
                    },
                    timeframeEnd,
                    setTimeEnd = { value ->
                        timeframeEnd = value
                        val editor = sharedPreferences.edit()
                        editor.putString(Settings.BEDTIME_END.getKey(), value.toString())
                        editor.apply()
                    },
                    timeframeEnabled = useTimeframe,
                    setTimeframe = { value ->
                        useTimeframe = value
                        val editor = sharedPreferences.edit()
                        editor.putBoolean(Settings.BEDTIME_TIMEFRAME.getKey(), value)
                        editor.apply()
                    }
                )
            }
            composable(Routes.SETTINGS_BEDTIME_SENSOR.getRoute()) {
                WearBedtimeSensorSetting(
                    navigator = navController,
                    sensor = bedtimeSensor,
                    setSensor = { value ->
                        bedtimeSensor = value
                        val editor = sharedPreferences.edit()
                        editor.putString(Settings.BEDTIME_SENSOR.getKey(), value.toString())
                        editor.apply()
                    }
                )
            }
            composable(Routes.TIME_PICKER.getRoute()){
                TimePicker(
                    close = {
                        navController.popBackStack()
                    },
                    userWakeTime,
                    setTime = { value ->
                        // Set the wake time ONLY if
                        // it's user defined
                        if(wakeTime.second === AlarmType.USER_DEFINED)
                            wakeTime = Pair(value, AlarmType.USER_DEFINED)

                        userWakeTime = value
                        val editor = sharedPreferences.edit()
                        editor.putString(Settings.WAKE_TIME.getKey(), value.toString())
                        editor.apply()
                    }
                )
            }
            composable(Routes.SETTINGS_TIMEFRAME_START.getRoute()){
                TimePicker(
                    close = {
                        navController.popBackStack()
                    },
                    timeframeStart,
                    setTime = { value ->
                        timeframeStart = value
                        val editor = sharedPreferences.edit()
                        editor.putString(Settings.BEDTIME_START.getKey(), value.toString())
                        editor.apply()
                    }
                )
            }
            composable(Routes.SETTINGS_TIMEFRAME_END.getRoute()){
                TimePicker(
                    close = {
                        navController.popBackStack()
                    },
                    timeframeEnd,
                    setTime = { value ->
                        timeframeEnd = value
                        val editor = sharedPreferences.edit()
                        editor.putString(Settings.BEDTIME_END.getKey(), value.toString())
                        editor.apply()
                    }
                )
            }
            composable(Routes.SETTINGS_TIMEFRAME_END.getRoute()){
                TimePicker(
                    close = {
                        navController.popBackStack()
                    },
                    timeframeEnd,
                    setTime = { value ->
                        timeframeEnd = value
                        val editor = sharedPreferences.edit()
                        editor.putString(Settings.BEDTIME_END.getKey(), value.toString())
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
                    item = if(it.arguments?.getString("id")!! == "ALL") Item.StringItem("ALL") else Item.LocalDateTimeItem(
                        LocalDateTime.parse(it.arguments?.getString("id")!!)
                    ),
                    navigation = navController,
                    onDelete = { time ->
                        val mutated = history.toMutableSet()
                        if(time is Item.LocalDateTimeItem){
                            mutated.removeIf { date ->
                                date?.first?.isEqual(time.value) ?: false
                            }
                        } else if(time is Item.StringItem){
                            mutated.clear()
                        }

                        history = mutated

                        coroutineScope.launch {
                            lastBedtime = bedtimeViewModel.getLatest()
                        }
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