package com.turtlepaw.cats.presentation.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.RadioButtonDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.cats.database.ThemeViewModel
import com.turtlepaw.cats.database.colorsMap
import com.turtlepaw.cats.database.colorsNames
import com.turtlepaw.cats.presentation.components.Page

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ThemePicker(themeViewModel: ThemeViewModel) {
    val currentTheme = themeViewModel.currentTheme.collectAsState().value
    Page {
        item {
            Text(
                text = "Theme",
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }
        colorsMap.map {
            item {
                ToggleChip(
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedEndBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                            .compositeOver(MaterialTheme.colors.surface),

                    ),
                    checked = it.value == currentTheme,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            themeViewModel.setTheme(it.value)
                        }
                    },
                    label = {
                        Text(
                            text = colorsNames[it.key] ?: "Unknown",
                            modifier = Modifier
                                .padding(start = 5.dp)
                        )
                    },
                    toggleControl = {
                        RadioButton(
                            selected = it.value == currentTheme,
                            colors = RadioButtonDefaults.colors(
                                selectedDotColor = Color.White,
                                selectedRingColor = Color.White
                            )
                        )
                    },
                    appIcon = {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            it.value.primary,
                                            it.value.secondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(100f)
                                )
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun ThemePickerPreview() {
    //ThemePicker()
}