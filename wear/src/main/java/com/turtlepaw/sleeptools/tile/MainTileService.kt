package com.turtlepaw.sleeptools.tile

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.ImageDimension
import androidx.wear.protolayout.DimensionBuilders.SpProp
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.ColorFilter
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.LayoutElementBuilders.Row
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
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.SuspendingTileService
import com.turtlepaw.sleeptools.R
import com.turtlepaw.sleeptools.utils.AlarmsManager
import com.turtlepaw.sleeptools.utils.Settings
import com.turtlepaw.sleeptools.utils.SettingsBasics
import com.turtlepaw.sleeptools.utils.SleepQuality
import com.turtlepaw.sleeptools.utils.TimeDifference
import com.turtlepaw.sleeptools.utils.TimeManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter


private const val RESOURCES_VERSION = "1"
private const val DEFAULT_GOAL = 8 // 8hrs
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
class MainTileService : SuspendingTileService() {
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
        // Initialize managers
        val timeManager = TimeManager()
        val alarmManager = AlarmsManager()
        // Get preferences
        val useAlarm = sharedPreferences.getBoolean(Settings.ALARM.getKey(), Settings.ALARM.getDefaultAsBoolean())
        val wakeTimeString = sharedPreferences.getString(Settings.WAKE_TIME.getKey(), Settings.WAKE_TIME.getDefault())
        // Get next alarm
        val nextAlarm = alarmManager.fetchAlarms(this);
        // Calculate wake time
        // between alarm and wake time
        val wakeTime = timeManager.getWakeTime(
            useAlarm,
            nextAlarm,
            wakeTimeString,
            Settings.WAKE_TIME.getDefaultAsLocalTime()
        );
        // Calculate time difference
        val sleepTime = timeManager.calculateTimeDifference(wakeTime.first)
        // Calculate sleep quality from time diff
        val sleepQuality = timeManager.calculateSleepQuality(sleepTime)

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
                                sleepTime,
                                sleepQuality,
                                wakeTime.first
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
}

private fun tileLayout(
    context: Context,
    sleepTime: TimeDifference,
    sleepQuality: SleepQuality,
    wakeTime: LocalTime
): EdgeContentLayout.Builder {
    val formatter = DateTimeFormatter.ofPattern("h:mma")
    val deviceParameters = buildDeviceParameters(context.resources)
    return EdgeContentLayout.Builder(deviceParameters)
        .setEdgeContent(
            CircularProgressIndicator.Builder()
                .setProgress(sleepTime.hours.toFloat() / DEFAULT_GOAL)
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
            Text.Builder(context, "Sleep")
                .setTypography(6.toInt())
                .setColor(argb(TileColors.LightText))
                .build()
        )
        .setSecondaryLabelTextContent(
            LayoutElementBuilders.Column.Builder()
                .addContent(
                    Text.Builder(context, "${formatter.format(LocalTime.now())}-${formatter.format(wakeTime)}")
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
                        .setText(sleepTime.hours.toString())
                        .setFontStyle(
                            FontStyle.PrimaryFontSize.getBuilder()
                        )
                        .build()
                )
                .addSpan(
                    SpanText.Builder()
                        .setText("h")
                        .setFontStyle(
                            FontStyle.SecondaryFontSize.getBuilder()
                        )
                        .build()
                )
                .addSpan(
                    SpanText.Builder()
                        .setText(" ")
                        .build()
                )
                .addSpan(
                    SpanText.Builder()
                        .setText(sleepTime.minutes.toString())
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
    val timeDifference = timeManager.calculateTimeDifference(LocalTime.MIDNIGHT);
    val sleepQuality = timeManager.calculateSleepQuality(timeDifference)

    LayoutRootPreview(root = tileLayout(
        LocalContext.current,
        timeDifference,
        sleepQuality,
        LocalTime.NOON,
    ).build())
}