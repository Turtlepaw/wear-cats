package com.turtlepaw.sleeptools.presentation.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.composables.TimePickerWith12HourClock
import com.turtlepaw.sleeptools.presentation.theme.SleepTheme
import com.turtlepaw.sleeptools.utils.Settings
import java.time.LocalTime

@Composable
fun WakeTimePicker(
    closePicker: () -> Unit,
    userWakeTime: LocalTime,
    setWakeTime: (value: LocalTime) -> Unit
){
    SleepTheme {
        TimePickerWith12HourClock(
            time = userWakeTime,
            onTimeConfirm = { time ->
                setWakeTime(time)
                closePicker()
            }
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun WakeTimePickerPreview() {
    WakeTimePicker(
        closePicker = {},
        userWakeTime = Settings.WAKE_TIME.getDefaultAsLocalTime(),
        setWakeTime = {}
    )
}