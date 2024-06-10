package com.turtlepaw.cats.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ResponsiveTimeText
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.layout.scrollAway

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Page(
    rotaryMode: ScalingLazyColumnState.RotaryMode = ScalingLazyColumnState.RotaryMode.Scroll,
    content: ScalingLazyListScope.() -> Unit
) {
    val columnState = rememberColumnState(
        ScalingLazyColumnDefaults.responsive(
            rotaryMode = rotaryMode,
            hapticsEnabled = true,
        ),
    )

    Scaffold(
        timeText = {
            ResponsiveTimeText(modifier = Modifier.scrollAway(columnState))
        },
        positionIndicator = {
            PositionIndicator(columnState.state)
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        }
    ) {
        ScalingLazyColumn(
            columnState = columnState,
            content = content
        )
    }
}