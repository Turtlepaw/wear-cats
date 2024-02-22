package com.turtlepaw.sunlight.presentation.pages

import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.sunlight.presentation.components.ItemsListWithModifier
import com.turtlepaw.sunlight.presentation.theme.SleepTheme
import com.turtlepaw.sunlight.services.LightLoggerService
import com.turtlepaw.sunlight.services.LightWorker
import com.turtlepaw.sunlight.services.SensorReceiver


@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun ClockworkToolkit(
    light: Float,
    context: Context
){
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val sensorWorker = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, SensorReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
//        val lightWorker = PendingIntent.getBroadcast(
//            context,
//            0,
//            Intent(context, LightLoggerService::class.java),
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
//        )
        val lightWorker = isServiceRunning(LightWorker::class.java, context)
        val isSampling = isServiceRunning(LightLoggerService::class.java, context)
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
                    Text(text = "Clockwork")
                }
                item {
                    Text(
                        text = "Current Light",
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    Text(text = "$light lx")
                }
                item {
                    Text(
                        text = "Light worker",
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    Text(
                        text = if(lightWorker) "Running"
                        else if(sensorWorker != null) "Idle"
                        else if(isSampling) "Sampling"
                        else "Not running",
                        color = if(lightWorker) Color.Green
                        else if(sensorWorker != null) Color.Yellow
                        else if(isSampling) Color.Blue
                        else Color.Red
                    )
                }
            }
        }
    }
}

@SuppressWarnings("deprecation")
private fun isServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun ToolkitPreview() {
    ClockworkToolkit(
        light = 2000f,
        context = LocalContext.current
    )
}