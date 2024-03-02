package com.turtlepaw.cats.tile

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.SpanText
import androidx.wear.protolayout.LayoutElementBuilders.Spannable
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ModifiersBuilders.Padding
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.CircularProgressIndicator
import androidx.wear.protolayout.material.ProgressIndicatorColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.SuspendingTileService
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.dataStore
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.SunlightViewModel
import com.turtlepaw.cats.utils.SunlightViewModelFactory
import com.turtlepaw.cats.utils.TimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs


private const val RESOURCES_VERSION = "1"
const val DEFAULT_GOAL = 8 // 8hrs
private const val LAUNCH_APP_ID = "LAUNCH_APP"
enum class Images(private val id: String) {
    SLEEP_QUALITY("sleep_quality");

    fun getId(): String {
        return id
    }
}

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService(), ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val TAG = "MainTileService"
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                Images.SLEEP_QUALITY.getId(),
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                        .setResourceId(R.drawable.sleep_quality)
                        .build()
                    ).build()
            ).build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val lastClickableId = requestParams.currentState.lastClickableId
        if (lastClickableId == LAUNCH_APP_ID) {
            Log.d("Tile", "Launching main activity...")
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
        }
        
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        val sunlightViewModel = ViewModelProvider(this, SunlightViewModelFactory(this.dataStore)).get(
            SunlightViewModel::class.java)
        // Get preferences
        val goal = sharedPreferences.getInt(Settings.GOAL.getKey(), Settings.GOAL.getDefaultAsInt())
        val today = sunlightViewModel.getDay(LocalDate.now())?.second ?: 0

        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(
                    // https://stackoverflow.com/a/77947118/15751555
                    LayoutElementBuilders.Box.Builder()
                        .setWidth(expand())
                        .setHeight(expand())
                        .setModifiers(
                            Modifiers.Builder()
                                .setClickable(
                                    ModifiersBuilders.Clickable.Builder()
                                        .setId(LAUNCH_APP_ID)
                                        .setOnClick(
                                            ActionBuilders.LoadAction.Builder()
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .addContent(
                            tileLayout(
                                this,
                                today,
                                goal
                            )
                                .build()
                        )
                        .build()
                ).build()
            ).build()
        ).build()


        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline)
            .setFreshnessIntervalMillis(
                // Every 5 minutes (60000 = 1m)
                60000 * 5
            )
            .build()
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        serviceScope.launch {
            try {
                getUpdater(this@MainTileService).requestUpdate(MainTileService::class.java)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to request tile update on enter", e)
            }
        }
    }
}

private fun tileLayout(
    context: Context,
    today: Int,
    goal: Int
): EdgeContentLayout.Builder {
    val formatter = DateTimeFormatter.ofPattern("h:mma")
    val deviceParameters = buildDeviceParameters(context.resources)
    return EdgeContentLayout.Builder(deviceParameters)
        .setEdgeContent(
            CircularProgressIndicator.Builder()
                .setProgress(today.toFloat() / goal.toFloat())
                .setStartAngle(-170f)
                .setEndAngle(170f)
                .setCircularProgressIndicatorColors(
                    ProgressIndicatorColors(
                        TileColors.PrimaryColor,
                        TileColors.TrackColor
                    )
                )
                .build()
        )
        .setPrimaryLabelTextContent(
            Text.Builder(context, "Sunlight")
                .setTypography(6.toInt())
                .setColor(argb(TileColors.LightText))
                .build()
        )
        .setSecondaryLabelTextContent(
            LayoutElementBuilders.Column.Builder()
                .addContent(
                    Text.Builder(
                        context,
                        if(today >= goal) "Goal Reached" else "${abs(today - goal)}m to go"                    )
                        .setTypography(Typography.TYPOGRAPHY_BODY1)
                        .setColor(argb(TileColors.White))
                        .setModifiers(
                            Modifiers.Builder()
                                .setPadding(
                                    Padding.Builder()
                                        .setBottom(
                                            dp(20f)
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
//                .addContent(
//                    Text.Builder(context, sleepQuality.getTitle())
//                        .setTypography(Typography.TYPOGRAPHY_BODY2)
//                        .setColor(argb(TileColors.White))
//                        .build()
//                )
                .build()
        )
        .setContent(
            Spannable.Builder()
                .addSpan(
                    SpanText.Builder()
                        .setText(today.toString())
                        .setFontStyle(
                            FontStyle.PrimaryFontSize.getBuilder()
                        )
                        .build()
                )
                .addSpan(
                    SpanText.Builder()
                        .setText("m")
                        .setFontStyle(
                            FontStyle.SecondaryFontSize.getBuilder()
                        )
                        .build()
                )
//                .addSpan(
//                    SpanText.Builder()
//                        .setText(" ")
//                        .build()
//                )
//                .addSpan(
//                    SpanText.Builder()
//                        .setText(sleepTime.minutes.toString())
//                        .setFontStyle(
//                            FontStyle.PrimaryFontSize.getBuilder()
//                        )
//                        .build()
//                )
//                .addSpan(
//                    SpanText.Builder()
//                        .setText("m")
//                        .setFontStyle(
//                            FontStyle.SecondaryFontSize.getBuilder()
//                        )
//                        .build()
//                )
                .build()
        )
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TilePreview() {
    val timeManager = TimeManager()
    val timeDifference = timeManager.calculateTimeDifference(LocalTime.of(5, 0));
    val sleepQuality = timeManager.calculateSleepQuality(timeDifference)

    LayoutRootPreview(root = tileLayout(
        LocalContext.current,
        15,
        Settings.GOAL.getDefaultAsInt()
    ).build())
}