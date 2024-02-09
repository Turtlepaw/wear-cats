package com.turtlepaw.sleeptools.presentation.pages.settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.scrollAway
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.sleeptools.presentation.components.ItemsListWithModifier
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.turtlepaw.sleeptools.utils.BedtimeSensor

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
        /**
         * Configure bedtime settings such as use bedtime, sensor (charging or bedtime mode), and force timeframe
         */
fun WearBedtimeSensorSetting(
    navigator: NavHostController,
    sensor: BedtimeSensor,
    setSensor: (value: BedtimeSensor) -> Unit
){
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        var state by remember { mutableStateOf(sensor) }
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
                    Spacer(modifier = Modifier.padding(1.dp))
                }
                item {
                    Log.d("BedtimeSensorConfig", "Is bedtime enabled? ${state == BedtimeSensor.BEDTIME} Is charging enabled? ${state == BedtimeSensor.CHARGING} sensor at ${state}")
                    ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                start = 8.dp,
                                end = 8.dp
                            ),
                        checked = state == BedtimeSensor.BEDTIME,
                        onCheckedChange = {
                            setSensor(BedtimeSensor.BEDTIME)
                            state = BedtimeSensor.BEDTIME
                        },
                        label = {
                            Text("Bedtime Mode", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        toggleControl = {
                            RadioButton(
                                selected = state == BedtimeSensor.BEDTIME,
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
                            .padding(
                                top = 8.dp,
                                start = 8.dp,
                                end = 8.dp
                            ),
                        checked = state == BedtimeSensor.CHARGING,
                        onCheckedChange = {
                            setSensor(BedtimeSensor.CHARGING)
                            state = BedtimeSensor.CHARGING
                        },
                        label = {
                            Text("Charging", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        toggleControl = {
                            RadioButton(
                                selected = state == BedtimeSensor.CHARGING,
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