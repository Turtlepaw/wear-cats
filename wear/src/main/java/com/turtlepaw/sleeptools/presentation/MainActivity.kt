/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.sleeptools.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.MaterialTheme.colors
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.sleeptools.R
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.google.android.horologist.composables.TimePickerWith12HourClock
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.time.LocalTime
import java.time.Duration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val sharedPreferences = getSharedPreferences("SleepTurtlepawSettings", Context.MODE_PRIVATE)

        setContent {
            WearPages(sharedPreferences)
        }
    }
}

@Composable
fun WearPages(sharedPreferences: SharedPreferences){
    SleepTheme {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        val navController = rememberSwipeDismissableNavController()
        val wakeTimeString = sharedPreferences.getString("wake_time", "10:00") // Default to midnight
        val useAlarmString = sharedPreferences.getString("wake_time", "on") // Default to on
        val useAlarm = if (useAlarmString == null) {
            true
        } else {
            stringToBoolean(useAlarmString)
        }
        var wakeTime = try {
            LocalTime.parse(wakeTimeString)
        } catch (e: DateTimeParseException) {
            // Handle parsing error, use a default value, or show an error message
            LocalTime.NOON
        }
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                WearApp(
                    open = { route ->
                        navController.navigate(route)
                    },
                    wakeTime
                )
            }
            composable("settings") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    Button(
                        onClick = {
                            navController.navigate("date-picker")
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
//                    Row(
//                        horizontalArrangement = Arrangement.Center, // Center children horizontally
//                        verticalAlignment = Alignment.CenterVertically, // Center children vertically
//                        modifier = Modifier.fillMaxSize() // Fill the entire available space
//                    ) {
//                        Text(
//                            text = "Use Alarm",
//                            fontWeight = FontWeight.Medium,
//                            modifier = Modifier.weight(1f) // Takes up available space
//                        )
//                        Switch(
//                            checked = useAlarm,
//                            onCheckedChange = { newCheckedState ->
//
//                            },
//                            modifier = Modifier
//                                .wrapContentSize(),
//                            colors = SwitchDefaults.colors(
//                                checkedThumbColor = colors.primaryVariant, // Match the button color
//                                checkedTrackColor = colors.primary, // Match the button color
//                                uncheckedThumbColor = colors.onPrimary, // Match the text color
//                                uncheckedTrackColor = colors.onPrimary, // Match the text color
//                            )
//                        )
//                    }
                }
            }
            composable("date-picker"){
                TimePickerWith12HourClock(onTimeConfirm = { time ->
                    wakeTime = time
                    val editor = sharedPreferences.edit()
                    editor.putString("wake_time", time.toString())
                    editor.apply()
                    navController.navigate("settings")
                })
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearApp(open: (route: String) -> Unit, wakeTime: LocalTime) {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val formatter = DateTimeFormatter.ofPattern("hh:mm")
        val timeDifference = calculateTimeDifference(wakeTime)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            // not working
            TimeText(
                modifier = Modifier.scrollAway(scalingLazyListState)
            )
            ItemsListWithModifier(
                reverseDirection = true,
                modifier = Modifier
                    .rotaryWithScroll(
                        reverseDirection = true,
                        focusRequester = focusRequester,
                        scrollableState = scalingLazyListState,
                    ),
                scrollableState = scalingLazyListState,
            ) {
                item {
                    Button(
                        onClick = {
                            open("settings")
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
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sleep),
                            contentDescription = "sleep",
                            modifier = Modifier
                                .size(32.dp)
                                .padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Sleep Prediction",
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
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
                        Text(
                            text = "${wakeTime.format(formatter)} wake up",
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

data class TimeDifference(val hours: Long, val minutes: Long)
fun calculateTimeDifference(targetTime: LocalTime, now: LocalTime = LocalTime.now()): TimeDifference {
    val currentDateTime = LocalTime.now()
    val duration = if (targetTime.isBefore(now)) {
        // If the target time is before the current time, it's on the next day
        Duration.between(currentDateTime, targetTime).plusDays(1)
    } else {
        Duration.between(currentDateTime, targetTime)
    }

    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()
    return TimeDifference(hours, minutes)
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
        horizontalAlignment = Alignment.Start,
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
        open = {},
        wakeTime = LocalTime.of(10, 30)
    )
}