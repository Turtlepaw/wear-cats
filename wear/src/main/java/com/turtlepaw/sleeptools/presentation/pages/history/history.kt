package com.turtlepaw.sleeptools.presentation.pages.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.legend.Legend
import com.turtlepaw.sleeptools.presentation.Routes
import com.turtlepaw.sleeptools.presentation.components.ItemsListWithModifier
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Map
import kotlin.random.Random

fun getRandomEntries() = List(4) { entryOf(it, Random.nextFloat() * 16f) }
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
        val data = history.toList().asReversed().slice(7).associate { (dateString, yValue) ->
            LocalDate.parse(dateString) to yValue
        }
        val chartEntryModelProducer = ChartEntryModelProducer(
            List(history.size){
                entryOf(it, history.(it))
            }
        )

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
                    item {
                        Chart(
                            chart = columnChart(
                                spacing = 5.dp,
                            ),
                            chartModelProducer = chartEntryModelProducer,
                            startAxis = rememberStartAxis(
                                label = textComponent {
                                    this.color = Color.White.toArgb()
                                }
                            ),
                        bottomAxis = rememberBottomAxis(
                            label = textComponent {
                                this.color = Color.White.toArgb()
                            },
                            valueFormatter = { value, chartValues ->
                                // get the entry at the given value index
                                // get the local date from the entry
                                val localDate = entry?.localDate
                                // get the day of week from the local date
                                val dayOfWeek = localDate?.dayOfWeek
                                // get the first letter of the day of week
                                val dayName = dayOfWeek?.getDisplayName(TextStyle.SHORT, Locale.getDefault())?.first()?.toString()
                                // return the day name or an empty string if null
                                dayName.orEmpty()
                            }
                        ),
                        runInitialAnimation = false,
                            modifier = Modifier
                                .height(100.dp)
                        )
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

fun getRandomTime(amount: Int): MutableSet<LocalDateTime> {
    val randomTimes = mutableSetOf<LocalDateTime>()

    repeat(amount) {
        val hour = Random.nextInt(0, 24)
        val minute = Random.nextInt(0, 60)
        val second = Random.nextInt(0, 60)

        val randomTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute, second))
        randomTimes.add(randomTime)
    }

    return randomTimes
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HistoryPreview() {
    WearHistory(
        navigate = NavHostController(LocalContext.current),
        history = getRandomTime(5),
        loading = false
    )
}