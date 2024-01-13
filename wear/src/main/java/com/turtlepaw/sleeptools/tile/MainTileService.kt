package com.turtlepaw.sleeptools.tile

import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ModifiersBuilders.Padding
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.SuspendingTileService
import com.turtlepaw.sleeptools.R
import com.turtlepaw.sleeptools.presentation.MainActivity
import java.time.Duration
import java.time.LocalTime
import android.content.SharedPreferences
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.wear.protolayout.StateBuilders
import androidx.wear.protolayout.expression.DynamicBuilders
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicDuration
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicString
import androidx.wear.tiles.TileService
import com.turtlepaw.sleeptools.presentation.TimeDifference
import com.turtlepaw.sleeptools.presentation.calculateTimeDifference
import java.time.format.DateTimeParseException


private const val RESOURCES_VERSION = "1"

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {
    companion object {
        const val KEY_WAKE_TIME = "wake_time"
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                "sleep_icon",
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                        .setResourceId(R.drawable.sleep)
                        .build()
                    ).build()
            ).build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val lastClickableId = requestParams.currentState.lastClickableId
        if (lastClickableId == "open-app-main") {
            Log.d("Open App", "Opening main activity...")
            TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(
                    Intent(this, MainActivity::class.java)
                )
                .startActivities()
        }

        val sharedPreferences = getSharedPreferences("SleepTurtlepawSettings", Context.MODE_PRIVATE)
        val wakeTimeStr = sharedPreferences.getString("wake_time", "10:00")
        val wakeTime = try {
            LocalTime.parse(wakeTimeStr)
        } catch (e: DateTimeParseException) {
            // Handle parsing error, use a default value, or show an error message
            LocalTime.NOON
        }
        val sleepTime = calculateTimeDifference(wakeTime)

//        getUpdater(this)
//            .requestUpdate(MainTileService::class.java);

        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(tileLayout(this, sleepTime)).build()
            ).build()
        ).build()


        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline)
            .setFreshnessIntervalMillis(60000 * 5)
            .build()
    }
}

private fun tileLayout(context: Context, sleepTime: TimeDifference): LayoutElementBuilders.LayoutElement {
    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setContent(
            myLayout(context, sleepTime)
        ).build()
}

private fun myLayout(context: Context, sleepTime: TimeDifference): LayoutElement =
    LayoutElementBuilders.Column.Builder()
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
            .setClickable(
                ModifiersBuilders.Clickable.Builder()
                .setId("open-app-main")
                .setOnClick(ActionBuilders.LoadAction.Builder().build())
                .build()
            ).build()
        )
        .addContent(
            Text.Builder(context, "Sleep Prediction")
                .setColor(argb(0xFFBDC7C5.toInt()))
                .setTypography(Typography.TYPOGRAPHY_BODY1)
                .build()
        )
        .addContent(
            Text.Builder(context, "${sleepTime.hours}h ${sleepTime.minutes}m")
                .setColor(argb(0xFFE4C6FF.toInt()))
                .setTypography(Typography.TYPOGRAPHY_DISPLAY3)
                .build()
        )
        .addContent(
            LayoutElementBuilders.Image.Builder()
                .setModifiers(
                    ModifiersBuilders.Modifiers.Builder()
                        .setPadding(
                            Padding.Builder()
                                .setTop(dp(10f))
                                .build()
                        )
                        .build()
                )
                .setWidth(dp(40f))
                .setHeight(dp(40f))
                .setResourceId("sleep_icon")
                .build()
        )
//        .addContent(
//            Chip.Builder(
//                context,
//                Clickable.Builder()
//                    .setId("open-app")
//                    .setOnClick(ActionBuilders.launchAction(
//                        ComponentName("com.turtlepaw.sleeptools", "MainActivity")
//                    )).build(),
//                buildDeviceParameters(reso)
//            )
//                .setContentDescription("More")
//                .build()
//        )
        .build()

fun PreferencesHandler(sharedPreferences: SharedPreferences, key: String, default: String = "unknown"): String {
    return sharedPreferences.getString(key, default) ?: default
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TilePreview() {
    LayoutRootPreview(root = tileLayout(LocalContext.current, calculateTimeDifference(LocalTime.NOON)))
}