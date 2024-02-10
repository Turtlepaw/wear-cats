package com.turtlepaw.sleeptools.complication

import android.app.PendingIntent
import android.content.Context
import android.graphics.drawable.Icon
import android.util.Log
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
import com.turtlepaw.sleeptools.tile.DEFAULT_GOAL
import com.turtlepaw.sleeptools.utils.AlarmsManager
import com.turtlepaw.sleeptools.utils.Settings
import com.turtlepaw.sleeptools.utils.SettingsBasics
import com.turtlepaw.sleeptools.utils.TimeDifference
import com.turtlepaw.sleeptools.utils.TimeManager


class MainComplicationService : SuspendingComplicationDataSourceService() {
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
            TimeDifference(7, 30),
            "8h",
            "Sleep Prediction",
            type,
            this
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
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

        return createComplicationData(
            sleepTime,
            "${sleepTime.hours}h",
            "Sleep Prediction",
            request.complicationType,
            this
        )
    }

    private fun createComplicationData(
        sleepTime: TimeDifference,
        text: String,
        contentDescription: String,
        type: ComplicationType,
        context: Context
    ): ComplicationData {
        Log.d("SleepComplication", "Rendering complication...")
        val monochromaticImage = MonochromaticImage.Builder(
            Icon.createWithResource(context, com.turtlepaw.sleeptools.R.drawable.sleep_white)
        ).build()
        val smallImage = SmallImage.Builder(
            Icon.createWithResource(context, com.turtlepaw.sleeptools.R.drawable.sleep_white),
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
                max = 24f,
                value = sleepTime.hours.toFloat() / DEFAULT_GOAL,
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
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}