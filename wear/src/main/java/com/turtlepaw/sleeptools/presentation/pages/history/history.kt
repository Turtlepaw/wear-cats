package com.turtlepaw.sleeptools.presentation.pages.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.sleeptools.presentation.Routes
import com.turtlepaw.sleeptools.presentation.components.ItemsListWithModifier
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHistory(
    navigate: NavHostController,
    history: Set<LocalDateTime?>,
    loading: Boolean
) {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val dayFormatter = DateTimeFormatter.ofPattern("E d")
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        val dayAndTimeFormatter = DateTimeFormatter.ofPattern("E d hh:mm a")

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
                if(loading){
                    item {
                        CircularProgressIndicator()
                    }
                }  else if(history.isEmpty()){
                    item {
                        Text(text = "No history")
                    }
                } else {
                    item {
                        Text(text = "Bedtime History")
                    }
                    item {
                        Text(text = "Click to delete an entry")
                    }
                    item {
                        Spacer(modifier = Modifier.padding(3.dp))
                    }
                    items(history.filterNotNull().toList().asReversed()) { time ->
                        Chip(onClick = { navigate.navigate(Routes.DELETE_HISTORY.getRoute(time.toString())) }, colors = ChipDefaults.chipColors(), border = ChipDefaults.chipBorder()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, // Add this line to align text vertically
                                modifier = Modifier.fillMaxWidth() // Adjust the modifier as needed
                            ) {
                                Text(
                                    text = dayFormatter.format(time),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.padding(6.dp))
                                Text(
                                    fontSize = 22.sp,
                                    text = timeFormatter.format(time)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}