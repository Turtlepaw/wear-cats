package com.turtlepaw.cats.complication

import android.app.PendingIntent
import android.content.Context
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
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.dataStore
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.ImageViewModel
import com.turtlepaw.cats.utils.ImageViewModelFactory
import java.time.LocalDate


class MainComplicationService : SuspendingComplicationDataSourceService(), ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
    private val supportedComplicationTypes = arrayOf(
        ComplicationType.SHORT_TEXT,
        ComplicationType.LONG_TEXT,
        ComplicationType.RANGED_VALUE
    )

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type !in supportedComplicationTypes) {
            return null
        }
        return createComplicationData(
            30,
            30,
            "30m",
            "Sleep Prediction",
            type,
            this
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {

        return createComplicationData(
            0,
            0,
            "m",
            "Sunlight",
            request.complicationType,
            this
        )
    }

    private fun createComplicationData(
        sunlightToday: Int,
        goal: Int,
        text: String,
        contentDescription: String,
        type: ComplicationType,
        context: Context
    ): ComplicationData {
        Log.d("SunComplication", "Rendering complication...")
        val monochromaticImage = MonochromaticImage.Builder(
            Icon.createWithResource(context, R.drawable.sunlight)
        ).build()
        val smallImage = SmallImage.Builder(
            Icon.createWithResource(context, R.drawable.sunlight),
            SmallImageType.ICON
        ).build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text).build(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build()
            )
                .setMonochromaticImage(monochromaticImage)
//                .setTitle(
//                    PlainComplicationText.Builder(contentDescription)
//                        .build()
//                )
                .setSmallImage(smallImage)
                .setTapAction(createActivityIntent(context))
                .build()
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text).build(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build()
            )
                .setMonochromaticImage(monochromaticImage)
//                .setTitle(
//                    PlainComplicationText.Builder(contentDescription)
//                        .build()
//                )
                .setSmallImage(smallImage)
                .setTapAction(createActivityIntent(context))
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                min = 0f,
                max = if(sunlightToday >= goal) sunlightToday.toFloat() else goal.toFloat(),
                value = sunlightToday.toFloat(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            )
                .setText(
                    PlainComplicationText.Builder(text).build()
                )
                .setMonochromaticImage(monochromaticImage)
                .setTapAction(createActivityIntent(context))
//                .setTitle(
//                    PlainComplicationText.Builder(contentDescription)
//                        .build()
//                )
                .setSmallImage(smallImage)
                .build()
//            ComplicationType.NO_DATA -> TODO()
//            ComplicationType.EMPTY -> TODO()
//            ComplicationType.NOT_CONFIGURED -> TODO()
            ComplicationType.MONOCHROMATIC_IMAGE -> MonochromaticImageComplicationData.Builder(
                monochromaticImage,
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            ).build()
            ComplicationType.SMALL_IMAGE -> SmallImageComplicationData.Builder(
                smallImage,
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            )
                .build()
//            ComplicationType.PHOTO_IMAGE -> TODO()
            else -> throw IllegalArgumentException("unknown complication type")
//            ComplicationType.GOAL_PROGRESS -> GoalProgressComplicationData.Builder(
//                value = number,
//                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
//                targetValue = 8f
//            ).build()
//            ComplicationType.WEIGHTED_ELEMENTS -> TODO()
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