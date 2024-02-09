package com.turtlepaw.sleeptools.presentation.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.scrollAway
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.sleeptools.R
import com.turtlepaw.sleeptools.presentation.Routes
import com.turtlepaw.sleeptools.presentation.components.ItemsListWithModifier
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
/**
 * Configure bedtime settings such as use bedtime, sensor (charging or bedtime mode), and force timeframe
 */
fun WearBedtimeSettings(
    navigator: NavHostController,
    startTime: LocalTime,
    setTimeStart: (value: LocalTime) -> Unit,
    endTime: LocalTime,
    setTimeEnd: (value: LocalTime) -> Unit,
    timeframeEnabled: Boolean,
    setTimeframe: (value: Boolean) -> Unit
){
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        var state by remember { mutableStateOf(timeframeEnabled) }
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
                verticalAlignment = Arrangement.spacedBy(
                    space = 4.dp,
                    alignment = Alignment.Top,
                )
            ) {
                item {
                    Text(text = "Bedtime Settings")
                }
                item {
                    Button(
                        onClick = {
                            navigator.navigate(Routes.SETTINGS_BEDTIME_SENSOR.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                start = 8.dp,
                                end = 8.dp
                            ),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE4C6FF)
                        )
                    ) {
                        Text(
                            text = "Sensor Source",
                            color = Color.Black
                        )
                    }
                }
                item {
                    ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        checked = state,
                        onCheckedChange = { isEnabled ->
                            setTimeframe(isEnabled)
                            state = isEnabled
                        },
                        label = {
                            Text("Timeframe", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        appIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_calendar_clock_24),
                                contentDescription = "calendar",
                                modifier = Modifier
                                    .size(24.dp)
                                    .wrapContentSize(align = Alignment.Center),
                            )
                        },
                        toggleControl = {
                            Switch(
                                checked = state,
                                enabled = true,
                                modifier = Modifier.semantics {
                                    this.contentDescription =
                                        if (state) "On" else "Off"
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
                item {
                    Button(
                        enabled = state,
                        onClick = {
                            navigator.navigate(Routes.SETTINGS_TIMEFRAME_START.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE4C6FF)
                        )
                    ) {
                        Text(
                            text = "${formatter.format(startTime)} start",
                            color = Color.Black
                        )
                    }
                }
                item {
                    Button(
                        enabled = state,
                        onClick = {
                            navigator.navigate(Routes.SETTINGS_TIMEFRAME_END.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                start = 8.dp,
                                end = 8.dp
                            ),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE4C6FF)
                        )
                    ) {
                        Text(
                            text = "${formatter.format(endTime)} end",
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}