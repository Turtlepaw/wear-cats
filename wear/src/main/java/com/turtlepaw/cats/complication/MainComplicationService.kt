package com.turtlepaw.cats.complication

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PhotoImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import coil.imageLoader
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.pages.safelyFetchAsync
import com.turtlepaw.cats.tile.loadImage
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.enumFromJSON
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class MainComplicationService : SuspendingComplicationDataSourceService(), ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
    private val supportedComplicationTypes = arrayOf(
        ComplicationType.SMALL_IMAGE,
        ComplicationType.PHOTO_IMAGE
    )

    private suspend fun getImage(): Bitmap {
        val animalTypes = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        ).getString(
            Settings.ANIMALS.getKey(),
            Settings.ANIMALS.getDefault()
        )
        val types = enumFromJSON(animalTypes)
        val catImages = safelyFetchAsync(1, types)
        val unprocessedImageData = imageLoader.loadImage(this, catImages.first().url, 500);
        if(unprocessedImageData != null){
            return getRoundedCroppedBitmap(unprocessedImageData)
        } else {
            throw Exception("Failed to load image")
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type !in supportedComplicationTypes) {
            return null
        }
        return createComplicationData(
            type,
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        return createComplicationData(
            request.complicationType,
            getImage()
        )
    }

    private fun createComplicationData(type: ComplicationType, image: Bitmap): ComplicationData {
        val contentDescription = "Image"
        val smallImage = SmallImage.Builder(
            Icon.createWithBitmap(image),
            SmallImageType.ICON
        ).build()

        return when (type) {
            ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
                smallImage,
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            ).setTapAction(createActivityIntent(this)).build()
            ComplicationType.PHOTO_IMAGE -> PhotoImageComplicationData.Builder(
                photoImage = Icon.createWithBitmap(image),
                PlainComplicationText.Builder(contentDescription).build(),
            ).setTapAction(createActivityIntent(this)).build()
            else -> throw IllegalArgumentException("unknown complication type")
        }
    }

    private fun createActivityIntent(context: Context): PendingIntent {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }
}