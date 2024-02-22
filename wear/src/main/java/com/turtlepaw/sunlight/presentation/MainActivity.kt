/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.sunlight.presentation

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.turtlepaw.sunlight.presentation.pages.ClockworkToolkit
import com.turtlepaw.sunlight.presentation.pages.StatePicker
import com.turtlepaw.sunlight.presentation.pages.WearHome
import com.turtlepaw.sunlight.presentation.pages.history.WearHistory
import com.turtlepaw.sunlight.presentation.pages.settings.WearSettings
import com.turtlepaw.sunlight.presentation.theme.SleepTheme
import com.turtlepaw.sunlight.services.SensorReceiver
import com.turtlepaw.sunlight.utils.LightConfiguration
import com.turtlepaw.sunlight.utils.Settings
import com.turtlepaw.sunlight.utils.SettingsBasics
import com.turtlepaw.sunlight.utils.SunlightViewModel
import com.turtlepaw.sunlight.utils.SunlightViewModelFactory
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime


enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    GOAL_PICKER("/goal-picker"),
    SUN_PICKER("/sun-picker"),
    HISTORY("/history"),
    CLOCKWORK("/clockwork-toolkit");

    fun getRoute(query: String? = null): String {
        return if(query != null){
            "$route/$query"
        } else route
    }
}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsBasics.HISTORY_STORAGE_BASE.getKey())

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sunlightViewModelFactory: MutableState<SunlightViewModelFactory>
    private lateinit var sunlightViewModel: MutableState<SunlightViewModel>
    private var lastUpdated = mutableStateOf(LocalTime.now())
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var sunlightLx = mutableStateOf(0f)
    private val tag = "MainSunlightActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )

        // Initialize your BedtimeViewModelFactory here
        sunlightViewModelFactory = mutableStateOf(
            SunlightViewModelFactory(dataStore)
        )

        // Use the factory to create the BedtimeViewModel
        sunlightViewModel = mutableStateOf(
            ViewModelProvider(this, sunlightViewModelFactory.value)[SunlightViewModel::class.java]
        )

        // Initialize Sensor Manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Register Sensor Listener
        sensorManager!!.registerListener(
            this,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        val receiver = SensorReceiver()
        // Start the alarm
        Log.d(tag, "Starting sunlight alarm")
        receiver.startAlarm(this)

        setContent {
            WearPages(
                sharedPreferences,
                sunlightViewModel.value,
                this,
                sunlightLx.value,
                lastUpdated
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "Refreshing database...")
        // Initialize your BedtimeViewModelFactory here
        sunlightViewModelFactory = mutableStateOf(
            SunlightViewModelFactory(dataStore)
        )

        // Use the factory to create the BedtimeViewModel
        sunlightViewModel = mutableStateOf(
            ViewModelProvider(this, sunlightViewModelFactory.value)[SunlightViewModel::class.java]
        )

        lastUpdated = mutableStateOf(
            LocalTime.now()
        )

        // Register Sensor Listener
        sensorManager!!.registerListener(
            this,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        Log.d(tag, "Database refreshed")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister Sensor Listener to avoid memory leaks
        sensorManager?.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Unregister Sensor Listener to avoid memory leaks
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.d(tag, "Received light change")
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val luminance = event.values[0]
            // Check if light intensity surpasses threshold
            sunlightLx.value = luminance
        }
    }
}

@Composable
fun WearPages(
    sharedPreferences: SharedPreferences,
    sunlightViewModel: SunlightViewModel,
    context: Context,
    sunlightLx: Float,
    lastUpdated: MutableState<LocalTime>
){
    SleepTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        // Goal - the user's sun daily sun goal
        val goalInt = sharedPreferences.getInt(Settings.GOAL.getKey(), Settings.GOAL.getDefaultAsInt())
        val thresholdInt = sharedPreferences.getInt(Settings.SUN_THRESHOLD.getKey(), Settings.SUN_THRESHOLD.getDefaultAsInt())
        var goal by remember { mutableStateOf(goalInt) }
        var threshold by remember { mutableStateOf(thresholdInt) }
        // Sunlight
        var sunlightHistory by remember { mutableStateOf<Set<Pair<LocalDate, Int>?>>(emptySet()) }
        var sunlightToday by remember { mutableStateOf<Int>(0) }
        var loading by remember { mutableStateOf(true) }
        // Suspended functions
        LaunchedEffect(key1 = sunlightViewModel, key2 = lastUpdated, key3 = lastUpdated.value) {
            Log.d("LaunchedEffectSun", "Updating (${lastUpdated.value})")
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
                        sunlightToday,
                        sunlightLx,
                        threshold
                    )
                }
            }
            composable(Routes.SETTINGS.getRoute()) {
                WearSettings(
                    navigate = { route ->
                        navController.navigate(route)
                    },
                    goal,
                    threshold
                )
            }
            composable(Routes.GOAL_PICKER.getRoute()){
                StatePicker(
                    List(60){
                        it.plus(1)
                    },
                    unitOfMeasurement = "m",
                    goal,
                ) { value ->
                    goal = value
                    val editor = sharedPreferences.edit()
                    editor.putInt(Settings.GOAL.getKey(), value)
                    editor.apply()
                    navController.popBackStack()
                }
            }
            composable(Routes.SUN_PICKER.getRoute()){
                StatePicker(
                    List(10){
                       it.times(1000)
                    },
                    unitOfMeasurement = "lx",
                    threshold,
                ) { value ->
                    threshold = value
                    val editor = sharedPreferences.edit()
                    editor.putInt(Settings.SUN_THRESHOLD.getKey(), value)
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
            composable(Routes.CLOCKWORK.getRoute()){
                ClockworkToolkit(light = sunlightLx, context = context)
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
        today = 30,
        5000f,
        LightConfiguration.LightThreshold.plus(5f).toInt()
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    WearSettings(
        navigate = {},
        goal = Settings.GOAL.getDefaultAsInt(),
        sunlightThreshold = Settings.SUN_THRESHOLD.getDefaultAsInt()
    )
}