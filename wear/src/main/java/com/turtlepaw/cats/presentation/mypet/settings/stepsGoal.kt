package com.turtlepaw.cats.presentation.mypet.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.cats.R

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun <T> StepGoalPicker(
    options: List<T>,
    renderer: (T) -> String = { it.toString() },
    currentState: T,
    recommendedItem: Int? = null,
    onSelect: (T) -> Unit
) {
    val initialIndex = options.indexOf(currentState)
    val state = rememberPickerState(
        initialNumberOfOptions = options.size,
        initiallySelectedOption = if (initialIndex != -1) initialIndex else 0
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        ListHeader {
//            Image(
//                asset = CommunityMaterial.Icon3.cmd_timer_cog,
//                contentDescription = stringResource(R.string.refresh_interval),
//                colorFilter = ColorFilter.tint(LocalContentColor.current)
//            )
//        }
        Picker(
            state = state,
            contentDescription = "Step Goal",
            modifier = Modifier
                .weight(1f)
                .rotaryScrollable(
                    behavior = RotaryScrollableDefaults.behavior(state),
                    focusRequester = rememberActiveFocusRequester()
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = renderer(options[it]),
                    style = with(LocalDensity.current) {
                        MaterialTheme.typography.display1.copy(
                            fontWeight = FontWeight.Medium,
                            // Ignore text scaling
                            fontSize = MaterialTheme.typography.display1.fontSize.value.dp.toSp()
                        )
                    },
                    color = MaterialTheme.colors.primary,
                    // In case of overflow, minimize weird layout behavior
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                if (it == recommendedItem) {
                    Text(
                        text = "(recommended)",
                        style = with(LocalDensity.current) {
                            MaterialTheme.typography.caption1.copy(
                                fontWeight = FontWeight.Medium,
                                // Ignore text scaling
                                fontSize = MaterialTheme.typography.caption1.fontSize.value.dp.toSp()
                            )
                        },
                        color = MaterialTheme.colors.onSurfaceVariant,
                        // In case of overflow, minimize weird layout behavior
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                }
            }
        }
        Button(
            onClick = {
                onSelect(options[state.selectedOption])
            },
            colors = ButtonDefaults.secondaryButtonColors(),
            modifier = Modifier
                .size(ButtonDefaults.DefaultButtonSize)
            //.wrapContentSize(align = Alignment.Center)
        ) {
            // Icon for history button
            Icon(
                painter = painterResource(id = R.drawable.check),
                tint = MaterialTheme.colors.primary,
                contentDescription = "Settings",
                modifier = Modifier
                    .padding(2.dp)
            )
        }
//        FilledIconButton(
//            onClick = { onSelectInterval(options[state.selectedOption]) },
//            modifier = Modifier.touchTargetAwareSize(IconButtonDefaults.SmallButtonSize)
//        ) {
//            Icon(
//                Icons.Filled.Check,
//                contentDescription = stringResource(id = R.string.save),
//                modifier = Modifier.size(IconButtonDefaults.iconSizeFor(IconButtonDefaults.SmallButtonSize))
//            )
//        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun PreviewRefreshIntervalPickerView() {
    CompositionLocalProvider {
        StepGoalPicker(
            currentState = 1,
            options = List(60){
                it.plus(1)
            },
            recommendedItem = null
        ) {}
    }
}