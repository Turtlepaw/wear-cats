package com.turtlepaw.cats.presentation.pages.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.CheckboxDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.scrollAway
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.components.ItemsListWithModifier
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.services.CatDownloadWorker
import com.turtlepaw.cats.utils.Animals
import com.turtlepaw.cats.utils.ImageViewModel
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.enumFromJSON
import com.turtlepaw.cats.utils.enumToJSON
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun WearSettings(
    context: Context,
    isConnected: Boolean,
    viewModel: ImageViewModel
) {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var isLoading by remember { mutableStateOf(false) }
        var isDownloaded by remember { mutableStateOf(false) }
        var animalsEnabled by remember { mutableStateOf<List<Animals>>(emptyList()) }
        val sharedPreferences = context.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        LaunchedEffect(true, isDownloaded) {
            val images = viewModel.getImages()
            isDownloaded = images.isNotEmpty()
            val animals = sharedPreferences.getString(
                Settings.ANIMALS.getKey(),
                Settings.ANIMALS.getDefault()
            )
            animalsEnabled = enumFromJSON(animals)
        }
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
                modifier = Modifier.rotaryWithScroll(
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
                    Text(text = "Downloads")
                }
                item {
                    Spacer(modifier = Modifier.padding(2.dp))
                }
                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                //viewModel.downloadImages(context)
                                val workRequest =
                                    OneTimeWorkRequestBuilder<CatDownloadWorker>().addTag(
                                        CatDownloadWorker.WORK_NAME
                                    ) // Add a unique tag to identify the work request
                                        .build()
                                val workManager = WorkManager.getInstance(context)
                                val work = workManager.enqueue(workRequest)
                                workManager.getWorkInfoByIdLiveData(workRequest.id)
                                    .observeForever { workInfo ->
                                        if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                            isLoading = false
                                        }
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        ),
                        enabled = !isLoading && isConnected
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.padding(end = 10.dp)) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        indicatorColor = MaterialTheme.colors.primary
                                    )
                                } else if (isDownloaded) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.check),
                                        contentDescription = "Downloaded for Offline",
                                        tint = MaterialTheme.colors.onPrimary,
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.offline_download),
                                        contentDescription = "Download for Offline",
                                        tint = MaterialTheme.colors.onPrimary,
                                    )
                                }
                            }
                            Text(
                                text = if (!isConnected) "Unavailable" else if (isLoading) "Downloading" else if (isDownloaded) "Downloaded" else "Download",
                                color = MaterialTheme.colors.onPrimary
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(2.dp))
                }
                item {
                    Text(text = "Animals")
                }
                item {
                    Spacer(modifier = Modifier.padding(2.dp))
                }
                items(Animals.entries.size) { current ->
                    val item = Animals.entries[current]
                    val itemState = animalsEnabled.any { it.name == item.name }
                    ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        checked = itemState,
                        onCheckedChange = { newState ->
                            if (animalsEnabled.size > 1 || newState) {
                                animalsEnabled = if (newState) {
                                    animalsEnabled.plus(item)
                                } else {
                                    animalsEnabled.filterNot { it.name == item.name }
                                }

                                val editor = sharedPreferences.edit()
                                editor.putString(
                                    Settings.ANIMALS.getKey(),
                                    enumToJSON(animalsEnabled)
                                )
                                editor.apply()
                            }
                        },
                        label = {
                            Text(
                                item.name.lowercase().replaceFirstChar {
                                    it.uppercase()
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        toggleControl = {
                            Checkbox(
                                checked = itemState,
                                enabled = true,
                                modifier = Modifier.semantics {
                                    this.contentDescription =
                                        if (itemState) "On" else "Off"
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedCheckmarkColor = MaterialTheme.colors.primary,
                                    checkedBoxColor = MaterialTheme.colors.primary
                                )
                            )
                        },
                        enabled = true,
                        colors = ToggleChipDefaults.toggleChipColors(
                            checkedEndBackgroundColor = MaterialTheme.colors.surface,
                            uncheckedEndBackgroundColor = MaterialTheme.colors.surface
                        )
                    )
                }
            }
        }
    }
}