/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.sleeptools.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.snapFlingBehavior
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.MaterialTheme.colors
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.scrollAway
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.TimePickerWith12HourClock
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.sleeptools.R
import com.turtlepaw.sleeptools.presentation.pages.WakeTimePicker
import com.turtlepaw.sleeptools.presentation.pages.WearHome
import com.turtlepaw.sleeptools.presentation.pages.WearSettings
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.turtlepaw.sleeptools.utils.AlarmType
import com.turtlepaw.sleeptools.utils.AlarmsManager
import com.turtlepaw.sleeptools.utils.Settings
import com.turtlepaw.sleeptools.utils.TimeManager
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    TIME_PICKER("/time-picker");

    fun getRoute(): String {
        return route
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val sharedPreferences = getSharedPreferences("SleepTurtlepawSettings", Context.MODE_PRIVATE)

        setContent {
            WearPages(sharedPreferences, this)
        }
    }
}

@Composable
fun WearPages(sharedPreferences: SharedPreferences, context: Context){
    SleepTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        // Creates a new alarm & time manager
        val timeManager = TimeManager();
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
        val nextAlarm = alarmManager.fetchAlarms(context);
        // Parses the wake time and decides if it should use
        // user defined or system defined
        var wakeTime = timeManager.getWakeTime(
            useAlarm,
            nextAlarm,
            wakeTimeString,
            Settings.WAKE_TIME.getDefaultAsLocalTime()
        );
        val userWakeTime = timeManager.parseTime(wakeTimeString, Settings.WAKE_TIME.getDefaultAsLocalTime());

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
                    timeManager
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
                        useAlarm = value;
                        val editor = sharedPreferences.edit()
                        editor.putBoolean(Settings.ALARM.getKey(), value)
                        editor.apply()
                    },
                    useAlarm,
                    setAlerts = { value ->
                        useAlerts = value;
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
                            wakeTime = Pair(value, AlarmType.USER_DEFINED);

                        val editor = sharedPreferences.edit()
                        editor.putString("wake_time", value.toString())
                        editor.apply()
                    }
                )
            }
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearHome(
        navigate = {},
        wakeTime = Pair(
            LocalTime.of(10, 30),
            AlarmType.SYSTEM_ALARM
        ),
        nextAlarm = LocalTime.of(7, 30),
        timeManager = TimeManager()
    )
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
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