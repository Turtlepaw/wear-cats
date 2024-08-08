package com.turtlepaw.cats.tile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tooling.preview.devices.WearDevices
import coil.imageLoader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.LayoutRootPreview
import com.google.android.horologist.tiles.SuspendingTileService
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.pages.safelyFetchAsync
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.enumFromJSON
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val RESOURCES_VERSION = "1"
const val LAUNCH_APP_ID = "LAUNCH_APP"
const val RESOURCE_VIGNETTE = "vignette"

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
            .setVersion(generateResourceVersion())
            .addIdToImageMapping(
                RESOURCE_REFRESH,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.cat_white)
                            .build()
                    ).build()
            )
            .addIdToImageMapping(
                RESOURCE_VIGNETTE,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.vignette)
                            .build()
                    ).build()
            )

        DataLoader().getImage(this).also {
            if (it != null) {
                resources.addIdToImageMapping(
                    getImageId(0),
                    it
                )
            } else throw Exception("Image failed to load")
        }

        return resources.build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val currentIndex = 0
        val lastClickableId = requestParams.currentState.lastClickableId
        if (lastClickableId == LAUNCH_APP_ID) {
            hapticClick(applicationContext)
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
        } else if (lastClickableId == MODIFIER_CLICK_REFRESH) {
            hapticClick(applicationContext)
            //startActivity(packageManager.getLaunchIntentForPackage(packageName))
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
            .setResourcesVersion(generateResourceVersion())
            .setTileTimeline(singleTileTimeline)
            .setFreshnessIntervalMillis(
                // Every hour (60000 = 1m, 1m * 60m = 1h)
                60000 * 60
            )
            .build()
    }

    private fun generateResourceVersion(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        return current.format(formatter)
    }
}

private fun layout(currentIndex: Int): LayoutElementBuilders.Box {
    return LayoutElementBuilders.Box.Builder()
        .setWidth(expand())
        .setHeight(expand())
        .setModifiers(
//            ModifiersBuilders.Modifiers.Builder()
//                .setClickable(
//                    ModifiersBuilders.Clickable.Builder()
//                        .setOnClick(
//                            ActionBuilders.LoadAction.Builder().build()
//                        )
//                        .setId(
//                            LAUNCH_APP_ID
//                        )
//                        .build()
//                )
//                .build()
            getRefreshModifiers(LAUNCH_APP_ID)
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
                .setContentScaleMode(LayoutElementBuilders.CONTENT_SCALE_MODE_CROP)
                .build()
        )
        .addContent(
            Image.Builder()
                .setResourceId(RESOURCE_VIGNETTE)
                .setWidth(
                    expand()
                )
                .setHeight(
                    expand()
                )
                .setContentScaleMode(LayoutElementBuilders.CONTENT_SCALE_MODE_CROP)
                .build()
        )
        .addContent(getRefreshButton())
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
    LayoutRootPreview(
        root = layout(
            1
        )
    )
}