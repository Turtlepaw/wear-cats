package com.turtlepaw.cats.presentation.pages.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.navigation.NavController
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
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.cats.R
import com.turtlepaw.cats.database.AppDatabase
import com.turtlepaw.cats.database.ThemeViewModel
import com.turtlepaw.cats.database.colorsMap
import com.turtlepaw.cats.presentation.Routes
import com.turtlepaw.cats.presentation.components.ItemsListWithModifier
import com.turtlepaw.cats.presentation.pages.WearHome
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.services.CatDownloadWorker
import com.turtlepaw.cats.utils.Animals
import com.turtlepaw.cats.utils.DOWNLOAD_LIMIT
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.enumFromJSON
import com.turtlepaw.cats.utils.enumToJSON
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID
import java.util.concurrent.TimeUnit

fun isWorkScheduled(context: Context, uniqueWorkName: String): Boolean {
    val workManager = WorkManager.getInstance(context)
    val statuses = workManager.getWorkInfosForUniqueWork(uniqueWorkName).get()
    for (workInfo in statuses) {
        if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
            return true
        }
    }
    return false
}

const val isOfflineAvailable = false

@SuppressLint("InlinedApi")
@OptIn(
    ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun WearSettings(
    context: Context,
    isConnected: Boolean,
    themeViewModel: ThemeViewModel,
    navController: NavController
) {
    val workManager = WorkManager.getInstance(context)
    val workRequest = remember {
        OneTimeWorkRequestBuilder<CatDownloadWorker>()
            .addTag(CatDownloadWorker.WORK_NAME)
            .build()
    }
    val focusRequester = rememberActiveFocusRequester()
    val scalingLazyListState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var animalsEnabled by remember { mutableStateOf<List<Animals>>(emptyList()) }
    var downloadProgress by remember { mutableStateOf<Int>(0) }
    val sharedPreferences = context.getSharedPreferences(
        SettingsBasics.SHARED_PREFERENCES.getKey(),
        SettingsBasics.SHARED_PREFERENCES.getMode()
    )
    val lastDownloadStr = sharedPreferences.getString(
        Settings.LAST_DOWNLOAD.getKey(),
        null
    )
    var lastDownload by remember {
        mutableStateOf(
            if (lastDownloadStr != null) {
                try {
                    LocalDate.parse(lastDownloadStr)
                } catch (e: DateTimeParseException) {
                    null
                }
            } else null
        )
    }
    val isAutoDownloadEnabled = isWorkScheduled(
        context,
        CatDownloadWorker.PERIODIC_WORK_NAME
    )
    var autoDownloadStatus by remember { mutableStateOf<Boolean>(isAutoDownloadEnabled) }
    var isDownloaded by remember { mutableStateOf(lastDownload != null) }
    LaunchedEffect(true, isDownloaded) {
//            val images = viewModel.getImages()
//            isDownloaded = images.isNotEmpty()
        val animals = sharedPreferences.getString(
            Settings.ANIMALS.getKey(),
            Settings.ANIMALS.getDefault()
        )
        animalsEnabled = enumFromJSON(animals)
    }

    var workId by remember { mutableStateOf<UUID?>(null) }
    val notificationPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    ) {
        workId = workRequest.id
        workManager.enqueue(workRequest)
    }
    DisposableEffect(workId) {
        val observer = Observer<WorkInfo> { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.RUNNING -> {
                    downloadProgress = workInfo.progress.getInt("Progress", 0)
                    isLoading = true
                }

                WorkInfo.State.SUCCEEDED -> {
                    downloadProgress = 0
                    isLoading = false
                    isDownloaded = true
                    lastDownload = LocalDate.now()
                }

                WorkInfo.State.FAILED -> {
                    coroutineScope.launch {
                        downloadProgress = 0
                        delay(2000)
                        workManager.enqueue(workRequest)
                        workId = workRequest.id
                    }
                }

                else -> {}
            }
        }

        val liveData = workManager.getWorkInfoByIdLiveData(workRequest.id)
        liveData.observeForever(observer)

        onDispose {
            liveData.removeObserver(observer)
        }
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
                space = 0.dp,
                alignment = Alignment.Top,
            )
        ) {
            if (isOfflineAvailable) {
                item {
                    Text(
                        text = "Downloads",
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                if (notificationPermissionState.status.isGranted) {
                                    workId = workRequest.id
                                    workManager.enqueue(workRequest)
                                } else notificationPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
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
                                    Box(modifier = Modifier.size(25.dp)) {
                                        if (downloadProgress == 0) {
                                            CircularProgressIndicator(
                                                indicatorColor = MaterialTheme.colors.primary
                                            )
                                        } else {
                                            CircularProgressIndicator(
                                                progress = (downloadProgress.toFloat() / DOWNLOAD_LIMIT.toFloat()),
                                                indicatorColor = MaterialTheme.colors.primary
                                            )
                                        }
                                    }
                                } else if (isDownloaded) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.check),
                                        contentDescription = "Downloaded for Offline",
                                        tint = if(!isConnected) MaterialTheme.colors.primary
                                                else MaterialTheme.colors.onPrimary,
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
                                color = if (isLoading || !isConnected) MaterialTheme.colors.primary else MaterialTheme.colors.onPrimary
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(4.dp))
                }
                item {
                    ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth(),
                        checked = autoDownloadStatus,
                        onCheckedChange = { newState ->
                            if (newState) {
                                val constraints = Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.UNMETERED)
                                    .setRequiresCharging(true)
                                    .build()

                                val periodicWorkRequest =
                                    PeriodicWorkRequestBuilder<CatDownloadWorker>(1, TimeUnit.DAYS)
                                        .setConstraints(constraints)
                                        .build()

                                workManager.enqueueUniquePeriodicWork(
                                    CatDownloadWorker.PERIODIC_WORK_NAME,
                                    ExistingPeriodicWorkPolicy.UPDATE,
                                    periodicWorkRequest
                                )
                                autoDownloadStatus = true
                            } else {
                                workManager.cancelUniqueWork(CatDownloadWorker.PERIODIC_WORK_NAME)
                                autoDownloadStatus = false
                            }
                        },
                        label = {
                            Text(
                                "Auto Download",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        toggleControl = {
                            Switch(
                                checked = autoDownloadStatus,
                                enabled = true,
                                modifier = Modifier.semantics {
                                    this.contentDescription =
                                        if (autoDownloadStatus) "On" else "Off"
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colors.primary,
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
                if (lastDownload != null) {
                    item {
                        val dateFormatter = DateTimeFormatter.ofPattern("E d")
                        Text(
                            text = "Last Downloaded\n${dateFormatter.format(lastDownload)}",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )
                    }
                }
            }
            item {
                Text(
                    text = "Animals",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(Animals.entries.size) { current ->
                val item = Animals.entries[current]
                val itemState = animalsEnabled.any { it.name == item.name }
                ToggleChip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
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

            item {
                Text(
                    text = "Customization",
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        navController.navigate(Routes.THEME_PICKER.getRoute())
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.palette),
                            contentDescription = "Palette",
                            tint = MaterialTheme.colors.onPrimary,
                            modifier = Modifier
                                .padding(2.dp)
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(
                            text = "Theme",
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            }
        }
    }
}