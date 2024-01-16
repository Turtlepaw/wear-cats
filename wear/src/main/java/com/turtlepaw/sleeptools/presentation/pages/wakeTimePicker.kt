package com.turtlepaw.sleeptools.presentation.pages

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import com.google.android.horologist.composables.TimePickerWith12HourClock
import com.turtlepaw.sleeptools.utils.AlarmType
import java.time.LocalTime

@Composable
fun WakeTimePicker(
    closePicker: () -> Unit,
    userWakeTime: LocalTime,
    setWakeTime: (value: LocalTime) -> Unit
){
    MaterialTheme(
        colors = Colors(
            primary = Color(0xFFE4C6FF),
            secondary = Color(0xFFE4C6FF)
        )
    ) {
        TimePickerWith12HourClock(
            time = userWakeTime,
            onTimeConfirm = { time ->
                setWakeTime(time)
                closePicker()
            }
        )
    }
}