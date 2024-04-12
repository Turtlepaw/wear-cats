package com.turtlepaw.cats.tile

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tooling.preview.devices.WearDevices
import coil.imageLoader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.compose.tools.buildDeviceParameters
import com.google.android.horologist.tiles.SuspendingTileService
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.pages.safelyFetchAsync
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.TimeManager
import com.turtlepaw.cats.utils.enumFromJSON
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalTime


private const val RESOURCES_VERSION = "1"
private const val LAUNCH_APP_ID = "LAUNCH_APP"

fun getImageId(index: Int): String {
    return "image_$index"
}

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {
    companion object {
        private const val MAX_LIMIT = 1
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        val animalTypes = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        ).getString(
            Settings.ANIMALS.getKey(),
            Settings.ANIMALS.getDefault()
        )
        val types = enumFromJSON(animalTypes)
        val resources = ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                RESOURCE_REFRESH,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.refresh)
                            .build()
                    ).build()
            )

        val catImages = safelyFetchAsync(MAX_LIMIT, types)
        val images = coroutineScope {
            catImages.map { image ->
                async {
                    val imageData = imageLoader.loadImage(this@MainTileService, image.url, 500)
                    imageData?.let { imageData }
                }
            }
        }.awaitAll().filterNotNull()

        images.forEachIndexed { index, data ->
            resources.addIdToImageMapping(
                getImageId(index),
                bitmapToImageResource(data)
            )
        }

        return resources.build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val currentIndex = 0
        val lastClickableId = requestParams.currentState.lastClickableId
        if (lastClickableId == LAUNCH_APP_ID) {
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
        } else if (lastClickableId.startsWith(MODIFIER_CLICK_REFRESH)) {
            hapticClick(applicationContext)
            // We can't have more than 1 image
            // since of android.os.TransactionTooLargeException
//            val index = parseIndexFromId(lastClickableId)
//            if(index != null && index == (MAX_LIMIT.minus(1))) {
//                currentIndex = 0
//            } else if(index != null){
//                currentIndex = index
//            }
        }

        val singleTileTimeline = TimelineBuilders.Timeline.Builder().addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder().setLayout(
                LayoutElementBuilders.Layout.Builder().setRoot(
                    // https://stackoverflow.com/a/77947118/15751555
                    layout(currentIndex)
                ).build()
            ).build()
        ).build()


        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline)
            .setFreshnessIntervalMillis(
                // Every hour (60000 = 1m, 1m * 60m = 1h)
                60000 * 60
            )
            .build()
    }
}

private fun layout(currentIndex: Int): LayoutElementBuilders.Box {
    return LayoutElementBuilders.Box.Builder()
        .setWidth(expand())
        .setHeight(expand())
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
                .setClickable(
                    ModifiersBuilders.Clickable.Builder()
                        .setOnClick(
                            ActionBuilders.LoadAction.Builder().build()
                        )
                        .setId(
                            LAUNCH_APP_ID
                        )
                        .build()
                )
                .build()
        )
        .addContent(
            Image.Builder()
                .setResourceId(
                    getImageId(currentIndex)
                )
                .setWidth(
                    expand()
                )
                .setHeight(
                    expand()
                )
                .build()
        )
        .build()
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

    LayoutRootPreview(
        root = layout(
            1
        )
    )
}