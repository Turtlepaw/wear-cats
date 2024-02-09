package com.turtlepaw.sleeptools.complication

import android.content.Context
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.turtlepaw.sleeptools.utils.TimeManager
import java.time.LocalTime
import java.time.format.DateTimeParseException

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
        return createComplicationData(8f,
            "8h",
            "Sleep",
            type
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val timeManager = TimeManager()
        val sharedPreferences = getSharedPreferences("SleepTurtlepawSettings", Context.MODE_PRIVATE)
        val wakeTimeString = sharedPreferences.getString("wake_time", "10:00")
        val wakeTime = try {
            LocalTime.parse(wakeTimeString)
        } catch (e: DateTimeParseException) {
            // Handle parsing error, use a default value, or show an error message
            LocalTime.NOON
        }
        val timeDifference = timeManager.calculateTimeDifference(wakeTime)
        val timeDifferenceInHours = timeDifference.hours / 3600.0f

        return createComplicationData(
            timeDifferenceInHours,
            "${timeDifference.hours}h",
            "Sleep",
            request.complicationType
        )
    }

    private fun createComplicationData(number: Float, text: String, contentDescription: String, type: ComplicationType): ComplicationData =
        when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text).build(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build()
            ).build()
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder(text).build(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build()
            )
            .build()
            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                min = 0f,
                max = 24f,
                value = number,
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            ).build()
            ComplicationType.NO_DATA -> TODO()
            ComplicationType.EMPTY -> TODO()
            ComplicationType.NOT_CONFIGURED -> TODO()
            ComplicationType.MONOCHROMATIC_IMAGE -> TODO()
            ComplicationType.SMALL_IMAGE -> TODO()
            ComplicationType.PHOTO_IMAGE -> TODO()
            else -> throw IllegalArgumentException("unknown complication type")
//            ComplicationType.GOAL_PROGRESS -> GoalProgressComplicationData.Builder(
//                value = number,
//                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
//                targetValue = 8f
//            ).build()
//            ComplicationType.WEIGHTED_ELEMENTS -> TODO()
        }
}