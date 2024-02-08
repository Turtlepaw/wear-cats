package com.turtlepaw.sleeptools.presentation.pages.history

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
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
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.ComponentShader
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.dimensions.Dimensions
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.legend.Legend
import com.turtlepaw.sleeptools.R
import com.turtlepaw.sleeptools.presentation.Routes
import com.turtlepaw.sleeptools.presentation.components.ItemsListWithModifier
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.Map
import kotlin.math.abs
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
                    val goal = history.filterNotNull().last()
                    val bottomAxisValueFormatter =
                        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { x, _ -> daysOfWeek[x.toInt() % daysOfWeek.size] }
//        val data = history.toMutableList().filterNotNull().asReversed().mapIndexed() { (index, date) ->
//            val bedtimeDifference = Duration.between(goal, date).toHours().toFloat()
//            return@map FloatEntry(index)
//        }.toList()
                    //val chartEntryModel = entryModelOf(data)
                    val maxValue = 10f
                    val currentWeekNumber = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                    val unfilteredWeek = history.filterNotNull().filter { sleepDate ->
                        val sleepWeekNumber = sleepDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                        sleepWeekNumber == currentWeekNumber
                    }

                    val thisWeek = unfilteredWeek.groupBy { it.toLocalDate() } // Group records by date only
                        .mapValues { (_, records) ->
                            records.maxByOrNull { it } ?: records.first() // Get the record closest to bedtime or the first record if there's only one
                        }.values
//        val data = List(thisWeek.size){ index ->
//            val date = thisWeek.elementAtOrNull(index) ?: return@List null
//            val bedtimeDifference = Duration.between(goal, date).toHours().toFloat()
//            entryOf(index.toFloat(), bedtimeDifference)
//        }.filterNotNull().toMutableList()

                    val rawData = List(7) { index ->
                        val date = thisWeek.elementAtOrNull(index)
                        if (date != null) {
                            val bedtimeDifference = Duration.between(goal, date).toHours().toFloat()
                            Log.d("Render", "Rendering ${dayFormatter.format(date)} as ${-bedtimeDifference}")
                            Pair(
                                false,
                                entryOf(index.toFloat(), abs(bedtimeDifference - maxValue))
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
                        Text(text = "Bedtime History")
                    }
                    item {
                        Spacer(modifier = Modifier.padding(3.dp))
                    }
                    item {
                        Chart(
                            chart = columnChart(
                                spacing = 2.dp,
                                columns = rawData.map { (missing, entry) ->
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
                        Text(text = "This chart shows how consistent you've been this week", textAlign = TextAlign.Center)
                    }
//                    item {
//                        Spacer(modifier = Modifier.padding(3.dp))
//                    }
//                    item {
//                        Text(text = "Click to delete an entry")
//                    }
                    item {
                        Spacer(modifier = Modifier.padding(3.dp))
                    }
                    items(history.filterNotNull().toList().asReversed()) { time ->
                        Chip(
                            onClick = { navigate.navigate(Routes.DELETE_HISTORY.getRoute(time.toString())) },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = MaterialTheme.colors.secondary
                            ),
                            border = ChipDefaults.chipBorder()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, // Add this line to align text vertically
                                modifier = Modifier.fillMaxWidth() // Adjust the modifier as needed
                            ) {
                                Text(
                                    text = dayFormatter.format(time),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.W500,
                                    color = MaterialTheme.colors.onSecondary
                                )
                                Spacer(modifier = Modifier.padding(6.dp))
                                Text(
                                    fontSize = 22.sp,
                                    text = timeFormatter.format(time),
                                    fontWeight = FontWeight.W500,
                                    color = MaterialTheme.colors.onSecondary
                                )
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = { navigate.navigate(Routes.DELETE_HISTORY.getRoute("ALL")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 8.dp,
                                    start = 2.dp,
                                    end = 2.dp
                                ),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFE4C6FF)
                            )
                        ) {
                            Text(
                                text = "Clear All",
                                color = Color.Black
                            )
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