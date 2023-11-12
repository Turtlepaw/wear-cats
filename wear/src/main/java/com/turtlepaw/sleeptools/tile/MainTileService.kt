package com.turtlepaw.sleeptools.tile

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

private const val RESOURCES_VERSION = "1"

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
        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(tileLayout(this)).build()
            ).build()
        ).build()

        return TileBuilders.Tile.Builder().setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline).build()
    }
}

private fun tileLayout(context: Context): LayoutElementBuilders.LayoutElement {
    return PrimaryLayout.Builder(buildDeviceParameters(context.resources))
        .setContent(
            myLayout(context)
        ).build()
}

private fun myLayout(context: Context): LayoutElement =
    LayoutElementBuilders.Column.Builder()
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
            .setClickable(
                ModifiersBuilders.Clickable.Builder()
                .setId("open-app-main")
                .setOnClick(
                    ActionBuilders.launchAction(
                    ComponentName("com.turtlepaw.sleeptools", ".presentation.MainActivity")
                )).build()
            ).build()
        )
        .addContent(
            Text.Builder(context, "Sleep Prediction")
                .setColor(argb(0xFFBDC7C5.toInt()))
                .setTypography(Typography.TYPOGRAPHY_BODY1)
                .build()
        )
        .addContent(
            Text.Builder(context, "8hr 35min")
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
                .setWidth(dp(35f))
                .setHeight(dp(35f))
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

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun TilePreview() {
    LayoutRootPreview(root = tileLayout(LocalContext.current))
}