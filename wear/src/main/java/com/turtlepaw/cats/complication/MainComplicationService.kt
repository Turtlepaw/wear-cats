package com.turtlepaw.cats.complication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PhotoImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.turtlepaw.cats.R
import com.turtlepaw.cats.services.ComplicationUpdater
import com.turtlepaw.cats.tile.DataLoader
import com.turtlepaw.cats.utils.decodeBitmapFromRawResource


class MainComplicationService : SuspendingComplicationDataSourceService(), ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
    private val supportedComplicationTypes = arrayOf(
        ComplicationType.SMALL_IMAGE,
        ComplicationType.PHOTO_IMAGE
    )

    private suspend fun getImage(): Bitmap {
        val data = DataLoader().getImageAsBitmap(this)
        if(data != null){
            return getRoundedCroppedBitmap(data)
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
            getRoundedCroppedBitmap(
                decodeBitmapFromRawResource(this, R.raw.preview) ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            )
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
        val intent = Intent(context, ComplicationUpdater::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }
}