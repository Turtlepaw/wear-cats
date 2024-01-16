package com.turtlepaw.sleeptools.presentation.pages

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.turtlepaw.sleeptools.presentation.components.ItemsListWithModifier
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.turtlepaw.sleeptools.utils.AlarmType
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun WearSettings(
    navigate: (route: String) -> Unit,
    openWakeTimePicker: () -> Unit,
    wakeTime: Pair<LocalTime, AlarmType>,
    userWakeTime: LocalTime,
    setAlarm: (value: Boolean) -> Unit,
    useAlarm: Boolean,
    setAlerts: (value: Boolean) -> Unit,
    alerts: Boolean
){
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
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
                    Text(text = "Settings")
                }
//                item {
//                    Spacer(
//                        modifier = Modifier.padding(
//                            top = 2.dp
//                        )
//                    )
//                }
                item {
                    Button(
                        onClick = {
                            openWakeTimePicker()
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
                            text = "Wake Time: ${userWakeTime.format(formatter)}",
                            color = Color.Black
                        )
                    }
                }
                item {
                    ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                start = 8.dp,
                                end = 8.dp
                            ),
                        checked = useAlarm,
                        onCheckedChange = { isEnabled ->
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
                item {
                    ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        checked = alerts,
                        onCheckedChange = { isEnabled ->
                            setAlerts(isEnabled)
                        },
                        label = {
                            Text("Notifications", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        appIcon = {
                            Icon(
                                painter = painterResource(id = if (alerts) R.drawable.alerts_on else R.drawable.alerts_off),
                                contentDescription = "alert",
                                modifier = Modifier
                                    .size(24.dp)
                                    .wrapContentSize(align = Alignment.Center),
                            )
                        },
                        toggleControl = {
                            Switch(
                                checked = alerts,
                                enabled = true,
                                modifier = Modifier.semantics {
                                    this.contentDescription =
                                        if (alerts) "On" else "Off"
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
    }
}