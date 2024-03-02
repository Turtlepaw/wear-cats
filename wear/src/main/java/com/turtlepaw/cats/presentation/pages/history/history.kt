package com.turtlepaw.cats.presentation.pages.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
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
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.turtlepaw.cats.presentation.components.ItemsListWithModifier
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.utils.Settings
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHistory(
    goal: Int,
    history: Set<Pair<LocalDate, Int>?>,
    loading: Boolean
) {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val dayFormatter = DateTimeFormatter.ofPattern("E d")
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

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
                    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                    val bottomAxisValueFormatter =
                        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { x, _ -> daysOfWeek[x.toInt() % daysOfWeek.size] }
                    val maxValue = 10f
                    val currentWeekNumber = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                    val unfilteredWeek = history.filterNotNull().filter { sleepDate ->
                        val sleepWeekNumber = sleepDate.first.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                        sleepWeekNumber == currentWeekNumber
                    }

                    val thisWeek = unfilteredWeek.groupBy { it.first } // Group records by date only
                        .mapValues { (_, records) ->
                            records.maxByOrNull { it.first } ?: records.first() // Get the record closest to bedtime or the first record if there's only one
                        }.values

                    val rawData = List(7) { index ->
                        val date = thisWeek.elementAtOrNull(index)
                        if (date != null) {
                            Pair(
                                false,
                                entryOf(index.toFloat(), date.second.toFloat())
                            )
                        } else {
                            Pair(
                                true,
                                entryOf(index.toFloat(), 0f)
                            )
                        }
                    }

                    val data = rawData.map { data -> data.second }

                    val chartEntryModelProducer = ChartEntryModelProducer(
                        data
                    )

                    item {
                        Text(text = "This Week")
                    }
                    item {
                        Spacer(modifier = Modifier.padding(3.dp))
                    }
                    item {
                        Chart(
                            chart = columnChart(
                                axisValuesOverrider = AxisValuesOverrider.fixed(
                                    maxY = goal.toFloat()
                                ),
                                spacing = 2.dp,
                                columns = rawData.map { (_) ->
                                    LineComponent(
                                        thicknessDp = 5f,
                                        shape = Shapes.roundedCornerShape(allPercent = 40),
                                        color = MaterialTheme.colors.primary.toArgb(),
                                    )
                                }
                            ),
                            chartModelProducer = chartEntryModelProducer,
                            startAxis = rememberStartAxis(
                                label = textComponent {
                                    this.color = MaterialTheme.colors.onBackground.toArgb()
                                },
                                guideline = LineComponent(
                                    thicknessDp = 0.5f,
                                    color = MaterialTheme.colors.surface.toArgb(),
                                ),
                            ),
                            bottomAxis = rememberBottomAxis(
                                label = textComponent {
                                    this.color = MaterialTheme.colors.onBackground.toArgb()
                                },
                                valueFormatter = bottomAxisValueFormatter,
                                axis = LineComponent(
                                    color = MaterialTheme.colors.surface.toArgb(),
                                    thicknessDp = 0.5f
                                )
                            ),
                            modifier = Modifier
                                .height(100.dp)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.padding(3.dp))
                    }
                    item {
                        Text(text = "This chart shows how much sunlight you've had this week", textAlign = TextAlign.Center)
                    }
//                    item {
//                        Spacer(modifier = Modifier.padding(3.dp))
//                    }
//                    item {
//                        Text(text = "Click to delete an entry")
//                    }
//                    item {
//                        Spacer(modifier = Modifier.padding(3.dp))
//                    }
//                    items(history.filterNotNull().toList().asReversed()) { time ->
//                        Chip(
//                            onClick = { navigate.navigate(Routes.DELETE_HISTORY.getRoute(time.first.toString())) },
//                            colors = ChipDefaults.chipColors(
//                                backgroundColor = MaterialTheme.colors.secondary
//                            ),
//                            border = ChipDefaults.chipBorder(),
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically, // Add this line to align text vertically
//                            ) {
//                                Icon(
//                                    painter = painterResource(id = if(time.second == BedtimeSensor.BEDTIME) R.drawable.bedtime else R.drawable.charging),
//                                    contentDescription = "History",
//                                    tint = Color(0xFFE4C6FF),
//                                    modifier = Modifier
//                                        .padding(2.dp)
//                                )
//
//                                Spacer(modifier = Modifier.padding(6.dp))
//
//                                Column(
//                                    modifier = Modifier.fillMaxWidth() // Adjust the modifier as needed
//                                ) {
//                                    Text(
//                                        text = dayFormatter.format(time.first),
//                                        fontSize = 22.sp,
//                                        fontWeight = FontWeight.W500,
//                                        color = MaterialTheme.colors.onSecondary
//                                    )
//                                    Text(
//                                        fontSize = 22.sp,
//                                        text = timeFormatter.format(time.first),
//                                        fontWeight = FontWeight.W500,
//                                        color = MaterialTheme.colors.onSecondary
//                                    )
//                                }
//                            }
//                        }
//                    }
//                    item {
//                        Button(
//                            onClick = { navigate.navigate(Routes.DELETE_HISTORY.getRoute("ALL")) },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(
//                                    top = 8.dp,
//                                    start = 2.dp,
//                                    end = 2.dp
//                                ),
//                            colors = ButtonDefaults.buttonColors(
//                                backgroundColor = Color(0xFFE4C6FF)
//                            )
//                        ) {
//                            Text(
//                                text = "Clear All",
//                                color = Color.Black
//                            )
//                        }
//                    }
                }
            }
        }
    }
}

fun getRandomTime(amount: Int): MutableSet<Pair<LocalDate, Int>> {
    val randomTimes = mutableSetOf<Pair<LocalDate, Int>>()

    repeat(amount) {
        val randomTime = LocalDate.now()
        randomTimes.add(
            Pair(
                randomTime,
                10
            )
        )
    }

    return randomTimes
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun HistoryPreview() {
    WearHistory(
        goal = Settings.GOAL.getDefaultAsInt(),
        history = getRandomTime(5),
        loading = false
    )
}