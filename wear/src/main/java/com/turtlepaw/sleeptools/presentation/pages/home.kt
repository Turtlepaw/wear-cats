package com.turtlepaw.sleeptools.presentation.pages

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.sleeptools.R
import com.turtlepaw.sleeptools.presentation.Routes
import com.turtlepaw.sleeptools.presentation.components.ItemsListWithModifier
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.turtlepaw.sleeptools.utils.AlarmType
import com.turtlepaw.sleeptools.utils.TimeManager
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome(
    navigate: (route: String) -> Unit,
    wakeTime: Pair<LocalTime, AlarmType>,
    nextAlarm: LocalTime,
    timeManager: TimeManager,
    lastBedtime: LocalTime?
) {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val formatter = DateTimeFormatter.ofPattern("hh:mm")
        var timeDifference by remember {
            mutableStateOf(timeManager.calculateTimeDifference(wakeTime.first))
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
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            TimeText(
                modifier = Modifier.scrollAway(scalingLazyListState)
            )
            PositionIndicator(
                scalingLazyListState = scalingLazyListState
            )
            ItemsListWithModifier(
                modifier = Modifier
                    .rotaryWithScroll(
                        reverseDirection = false,
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
                        text = "${nextAlarm.format(formatter)} wake up${if(wakeTime.second === AlarmType.SYSTEM_ALARM) " (alarm)" else ""}",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if(lastBedtime != null){
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
                            text = "You should go to bed at ${formatter.format(lastBedtime)} to be consistent",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                            //color = Color(0xFF939AA3)
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(vertical = 5.dp))
                }
                item {
                    Button(
                        onClick = {
                            navigate(Routes.SETTINGS.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
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
                    Text(
                        text = "Made with 💤 by turtlepaw",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(
                                top = 10.dp
                            )
                    )
                }
                item {
                    Spacer(
                        modifier = Modifier.padding(
                            bottom = 14.dp
                        )
                    )
                }
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