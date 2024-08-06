package com.turtlepaw.cats.tile

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders

const val RESOURCE_REFRESH = "refresh_icon"
const val MODIFIER_CLICK_REFRESH = "refresh_tile"
fun getRefreshId(): String {
    return LAUNCH_APP_ID
}

fun parseIndexFromId(id: String): Int? {
    val parts = id.split("_")
    if (parts.size == 2) {
        return parts[1].toIntOrNull()
    }
    return null
}

// The following code is from home assistant:
// https://github.com/home-assistant/android/

/** Performs a [VibrationEffect.EFFECT_CLICK] or equivalent on older Android versions */
fun hapticClick(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService<VibratorManager>()
        val vibrator = vibratorManager?.defaultVibrator
        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    } else {
        val vibrator = context.getSystemService<Vibrator>()
        @Suppress("DEPRECATION")
        vibrator?.vibrate(200)
    }
}

/**
 * An [LayoutElementBuilders.Arc] with a refresh button at the bottom (centered). When added, it is
 * expected that the TileService:
 * - handles the refresh action ([MODIFIER_CLICK_REFRESH]) in `onTileRequest`;
 * - adds a resource for [RESOURCE_REFRESH] in `onTileResourcesRequest`.
 */
fun getRefreshButton(): LayoutElementBuilders.Arc =
    LayoutElementBuilders.Arc.Builder()
        .setAnchorAngle(
            DimensionBuilders.DegreesProp.Builder(180f).build()
        )
        .addContent(
            LayoutElementBuilders.ArcAdapter.Builder()
                .setContent(
                    LayoutElementBuilders.Image.Builder()
                        .setResourceId(RESOURCE_REFRESH)
                        .setWidth(DimensionBuilders.dp(24f))
                        .setHeight(DimensionBuilders.dp(24f))
                        .setModifiers(getRefreshModifiers())
                        .build()
                )
                .setRotateContents(false)
                .build()
        )
        .build()

/** @return a modifier for tiles that represents a 'tap to refresh' [ActionBuilders.LoadAction] */
fun getRefreshModifiers(): ModifiersBuilders.Modifiers {
    return ModifiersBuilders.Modifiers.Builder()
        .setClickable(
            ModifiersBuilders.Clickable.Builder()
                .setOnClick(
                    ActionBuilders.LoadAction.Builder().build()
                )
                .setId(
                    getRefreshId()
                )
                .build()
        )
        .build()
}