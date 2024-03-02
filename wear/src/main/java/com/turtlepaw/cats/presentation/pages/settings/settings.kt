package com.turtlepaw.cats.presentation.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.Routes
import com.turtlepaw.cats.presentation.components.ItemsListWithModifier
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.utils.Settings

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun WearSettings(
    navigate: (route: String) -> Unit,
    goal: Int,
    sunlightThreshold: Int,
    isBatterySaver: Boolean,
    setBatterySaver: (state: Boolean) -> Unit
){
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
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
                item {
                    Button(
                        onClick = {
                            navigate(Routes.GOAL_PICKER.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text(
                            text = "${goal}m goal",
                            color = Color.Black
                        )
                    }
                }
                item {
                    Button(
                        onClick = {
                            navigate(Routes.SUN_PICKER.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text(
                            text = "$sunlightThreshold threshold",
                            color = Color.Black
                        )
                    }
                }
                item {
                    Button(
                        onClick = {
                            navigate(Routes.CLOCKWORK.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text(
                            text = "Toolkit",
                            color = Color.Black
                        )
                    }
                }
                item {
                    ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        checked = isBatterySaver,
                        onCheckedChange = { isEnabled ->
                            setBatterySaver(isEnabled)
                        },
                        label = {
                            Text("Battery Saver", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        appIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.battery_saver),
                                contentDescription = "battery saver",
                                modifier = Modifier
                                    .size(24.dp)
                                    .wrapContentSize(align = Alignment.Center),
                            )
                        },
                        toggleControl = {
                            Switch(
                                checked = isBatterySaver,
                                enabled = true,
                                modifier = Modifier.semantics {
                                    this.contentDescription =
                                        if (isBatterySaver) "On" else "Off"
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colors.primary
                                )
                            )
                        },
                        enabled = true,
                        colors = ToggleChipDefaults.toggleChipColors(
                            checkedEndBackgroundColor = MaterialTheme.colors.secondary
                        )
                    )
                }
                item {
                    Text(
                        text = "This app is open-source",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(
                                top = 10.dp
                            )
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    WearSettings(
        navigate = {},
        goal = Settings.GOAL.getDefaultAsInt(),
        sunlightThreshold = Settings.SUN_THRESHOLD.getDefaultAsInt(),
        isBatterySaver = true,
        setBatterySaver = {}
    )
}