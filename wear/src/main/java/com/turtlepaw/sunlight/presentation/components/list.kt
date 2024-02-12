package com.turtlepaw.sunlight.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState

@Composable
fun ItemsListWithModifier(
    reverseDirection: Boolean = false,
    modifier: Modifier,
    scrollableState: ScalingLazyListState,
    verticalAlignment: Arrangement.Vertical? = null,
    items: ScalingLazyListScope.() -> Unit
) {
    val flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = scrollableState)
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        state = scrollableState,
        reverseLayout = reverseDirection,
        flingBehavior = flingBehavior,
        scalingParams = ScalingLazyColumnDefaults.scalingParams(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = verticalAlignment ?: Arrangement.spacedBy(
            space = 4.dp,
            alignment = Alignment.Top,
        ),
        content = items
    )
}