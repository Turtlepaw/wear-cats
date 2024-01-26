package com.turtlepaw.sleeptools.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

@Composable
fun SleepTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        content = content,
        colors = Colors(
            primary = Color(0xFFE4C6FF),
            secondary = Color(0xFFE4C6FF),
            background = Color.Black
        )
    )
}