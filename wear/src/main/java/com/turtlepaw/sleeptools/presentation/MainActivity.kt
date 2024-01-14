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
import androidx.compose.foundation.layout.Column
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
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.turtlepaw.sleeptools.utils.TimeManager
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

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
        val navController = rememberSwipeDismissableNavController()
        val alarmManager = AlarmsManager()
        val wakeTimeString = sharedPreferences.getString("wake_time", "10:00") // Default to midnight
        val useAlarmBool = sharedPreferences.getBoolean("use_alarm", true) // Default to on
        var useAlarm by remember { mutableStateOf(useAlarmBool) }
        val nextAlarm = alarmManager.fetchAlarms(context);
        val timeManager = TimeManager();
        var wakeTime = timeManager.getWakeTime(useAlarm, nextAlarm, wakeTimeString);
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                WearApp(
                    navigate = { route ->
                        navController.navigate(route)
                    },
                    wakeTime,
                    nextAlarm = nextAlarm ?: wakeTime,
                    timeManager
                )
            }
            composable("settings") {
                WearSettings(
                    navigate = { route ->
                        navController.navigate(route)
                    },
                    wakeTime,
                    setAlarm = { value ->
                        useAlarm = value;
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("use_alarm", value)
                        editor.apply()
                    },
                    useAlarm
                )
            }
            composable("date-picker"){
                MaterialTheme(
                    colors = Colors(
                        primary = Color(0xFFE4C6FF),
                        secondary = Color(0xFFE4C6FF)
                    )
                ) {
                    TimePickerWith12HourClock(
                        onTimeConfirm = { time ->
                            wakeTime = time
                            val editor = sharedPreferences.edit()
                            editor.putString("wake_time", time.toString())
                            editor.apply()
                            navController.navigate("settings")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WearSettings(
    navigate: (route: String) -> Unit,
    wakeTime: LocalTime,
    setAlarm: (value: Boolean) -> Unit,
    useAlarm: Boolean
){
    SleepTheme {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        TimeText()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ){
            Button(
                onClick = {
                    navigate("date-picker")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFE4C6FF)
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sleep),
                    contentDescription = "sleep",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = "Wake Time: ${wakeTime.format(formatter)}",
                    color = Color.Black
                )
            }
            ToggleChip(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                checked = useAlarm,
                onCheckedChange = {isEnabled ->
                    setAlarm(isEnabled)
                },
                label = {
                    Text("Use Alarm", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                appIcon = {
                    Icon(
                        painter = painterResource(id = if (useAlarm) R.drawable.alarm_on else R.drawable.alarm_off),
                        contentDescription = "alarm",
                        modifier = Modifier
                            .size(24.dp)
                            .wrapContentSize(align = Alignment.Center),
                    )
                },
                toggleControl = {
                    Switch(
                        checked = useAlarm,
                        enabled = true,
                        modifier = Modifier.semantics {
                            this.contentDescription =
                                if (useAlarm) "On" else "Off"
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFE4C6FF)
                        )
                    )
                },
                enabled = true,
                colors = ToggleChipDefaults.toggleChipColors(
                    checkedEndBackgroundColor = Color(0x80E4C6FF)
                )
            )
        }
    }
}
@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearApp(
    navigate: (route: String) -> Unit,
    wakeTime: LocalTime,
    nextAlarm: LocalTime,
    timeManager: TimeManager
) {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val formatter = DateTimeFormatter.ofPattern("hh:mm")
        var timeDifference by remember {
            mutableStateOf(timeManager.calculateTimeDifference(wakeTime))
        }

        // Track the current minute
        var currentMinute by remember { mutableIntStateOf(LocalTime.now().minute) }

        // Use LaunchedEffect to launch a coroutine when the composable is first displayed
        LaunchedEffect(wakeTime) {
            val handler = Handler(Looper.getMainLooper())
            // Use a coroutine to run the code on the main thread
            while (true) {
                // Delay until the next minute
                delay(60_000 - (System.currentTimeMillis() % 60_000))

                // Update the current minute
                currentMinute = LocalTime.now().minute

                // Re-compose the composable
                handler.post {
                    timeDifference = timeManager.calculateTimeDifference(nextAlarm)
                    // we used to use wakeTime but now we use
                    // nextAlarm
                    // You can trigger a re-composition here, for example by updating some state
                    // or forcing a re-layout of your composable
                    // Uncomment the line below if your composable doesn't re-compose automatically
                    // currentMinute++
                }
            }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background),
                contentAlignment = Alignment.Center,
            ) {
                TimeText(
                    modifier = Modifier.scrollAway(scalingLazyListState)
                )
                PositionIndicator(
                    scalingLazyListState = scalingLazyListState
                )
                ItemsListWithModifier(
                    reverseDirection = false,
                    modifier = Modifier
                        .rotaryWithScroll(
                            reverseDirection = true,
                            focusRequester = focusRequester,
                            scrollableState = scalingLazyListState,
                        ),
                    scrollableState = scalingLazyListState,
                ) {
                    item {
                        Image(
                            painter = painterResource(id = R.drawable.sleep),
                            contentDescription = "sleep",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(bottom = 8.dp)
                        )
                    }
                    item {
                        Text(
                            text = "Sleep Prediction",
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    item {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontSize = 36.sp)) {
                                    append("${timeDifference.hours}")
                                }
                                append("hr ")
                                withStyle(style = SpanStyle(fontSize = 36.sp)) {
                                    append("${timeDifference.minutes}")
                                }
                                append("min")
                            },
                            color = Color(0xFFE4C6FF),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    item {
                        Text(
                            text = "${nextAlarm.format(formatter)} wake up",
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    item {
                        Text(
                            text = "Tip",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                            color = Color(0xFFE4C6FF)
                        )
                    }
                    item {
                        Text(
                            text = "You should go to bed at 1:35 AM to be consistent",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                            //color = Color(0xFF939AA3)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.padding(vertical = 5.dp))
                    }
                    item {
                        Button(
                            onClick = {
                                navigate("settings")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    bottom = 60.dp
                                ),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFE4C6FF)
                            )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sleep),
                                contentDescription = "sleep",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Settings",
                                color = Color.Black
                            )
                        }
                    }
                }
            }
    }
}

@Composable
fun ItemsListWithModifier(
    reverseDirection: Boolean = false,
    modifier: Modifier,
    scrollableState: ScalingLazyListState,
    items: ScalingLazyListScope.() -> Unit,
) {
    val flingBehavior = snapFlingBehavior(state = scrollableState)
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        state = scrollableState,
        reverseLayout = reverseDirection,
        flingBehavior = flingBehavior,
        scalingParams = scalingParams(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 4.dp,
            alignment = Alignment.Top,
        ),
        content = items,
    )
}

fun stringToBoolean(input: String): Boolean {
    val lowerCaseInput = input.lowercase(Locale.ROOT)
    return when (lowerCaseInput) {
        "true", "1", "yes", "on" -> true
        "false", "0", "no", "off" -> false
        else -> false
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(
        navigate = {},
        wakeTime = LocalTime.of(10, 30),
        nextAlarm = LocalTime.of(7, 30),
        timeManager = TimeManager()
    )
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    WearSettings(
        navigate = {},
        wakeTime = LocalTime.of(10, 30),
        setAlarm = {},
        useAlarm = true
    )
}