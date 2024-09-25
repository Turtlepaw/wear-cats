package com.turtlepaw.cats.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import androidx.annotation.Keep
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.IntervalDataPoint
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.turtlepaw.cats.mypet.CatStatus
import com.turtlepaw.cats.mypet.MoodManager
import com.turtlepaw.cats.mypet.Moods
import com.turtlepaw.cats.mypet.defaultStepGoal
import com.turtlepaw.cats.mypet.getCatStatusFlow
import com.turtlepaw.cats.mypet.getStepGoalFlow
import com.turtlepaw.cats.mypet.saveCatStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

@Keep
private const val MAX_TREATS_PER_DAY = 10 // Example cap

@Keep
class MyPetWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result = runBlocking {
        Log.d("MyPetWorker", "Starting...")
        if (context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MyPetWorker", "Permissions not granted...")
            return@runBlocking Result.retry()
        }

        val deferred = CompletableDeferred<Unit>()

        val healthClient = HealthServices.getClient(context)
        val passiveMonitoringClient = healthClient.passiveMonitoringClient

        val passiveListenerCallback = object : PassiveListenerCallback {
            override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                super.onNewDataPointsReceived(dataPoints)
                Log.d("MyPetWorker", "Steps changed")
                val steps = stepsFromDataPoint(
                    dataPoints.getData(DataType.STEPS_DAILY)
                )
                Log.d("MyPetWorker", "Steps are: $steps")

                // Update cat's status
                CoroutineScope(Dispatchers.IO).launch {
                    updateCatStatus(context, steps)
                    // Signal completion
                    deferred.complete(Unit)
                }
            }
        }

        val passiveListenerConfig = PassiveListenerConfig.builder()
            .setDataTypes(
                setOf(
                    DataType.STEPS_DAILY
                )
            )
            .build()

        passiveMonitoringClient?.setPassiveListenerCallback(
            passiveListenerConfig!!,
            passiveListenerCallback
        )

        Log.d("MyPetWorker", "Registered, waiting...")

        // Wait until data is received
        deferred.await()

        // Unregister measure callback
        withContext(Dispatchers.IO) {
            passiveMonitoringClient.clearPassiveListenerCallbackAsync()
        }

        Log.d("MyPetWorker", "All done!")
        return@runBlocking Result.success()
    }

    private fun stepsFromDataPoint(
        dataPoints: List<IntervalDataPoint<Long>>
    ): Long {
        var latest = 0
        var lastIndex = 0
        val bootInstant =
            Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime())

        if (dataPoints.isNotEmpty()) {
            dataPoints.forEachIndexed { index, intervalDataPoint ->
                val endTime = intervalDataPoint.getEndInstant(bootInstant)
                if (endTime.toEpochMilli() > latest) {
                    latest = endTime.toEpochMilli().toInt()
                    lastIndex = index
                }
            }

            return dataPoints[lastIndex].value
        } else return 0L
    }

    suspend fun updateCatStatus(context: Context, steps: Long) {
        val stepsGoal = getStepGoalFlow(context).firstOrNull() ?: defaultStepGoal
        val catStatus = getCatStatusFlow(context).firstOrNull() ?: CatStatus(
            hunger = 100,
            treats = 0,  // Available treats to feed the pet
            dailyTreatsUsed = 0,  // Ensure daily treats used starts at 0
            happinessReasons = mapOf(),
            happiness = 1,
            lastFed = null,
            lastUpdate = null
        )

        // Step 1: Calculate how many treats can be earned based on step goal completion
        val stepGoalCompletionRatio = (steps.toDouble() / stepsGoal.toDouble()).coerceAtMost(1.0)

        // Calculate how many treats *can* be given based on steps
        val treatsToAdd = (stepGoalCompletionRatio * MAX_TREATS_PER_DAY).toInt()

        // Step 2: Calculate how many treats remain today, ensure it doesn't exceed or go negative
        val treatsRemainingToday = (treatsToAdd - catStatus.dailyTreatsUsed).coerceAtLeast(0)

        Log.d("MyPetWorker", "Treats remaining today: $treatsRemainingToday")

        // Available treats should be based on how many treats the user can feed now
        val availableTreats = treatsRemainingToday.coerceAtLeast(0)

        // Ensure that treats are not fed automatically; this logic should only be invoked explicitly by user action
        // Treats fed should be triggered by a separate user interaction event
        val treatsFed = 0 // Avoid feeding treats unless explicitly instructed

        // Step 3: Update the total number of treats used today
        val updatedDailyTreatsUsed = (catStatus.dailyTreatsUsed + treatsFed).coerceAtMost(MAX_TREATS_PER_DAY)

        // After feeding, reset treats to 0 if necessary
        val updatedTreats = availableTreats - treatsFed

        Log.d("MyPetWorker", "Treats to add: $treatsToAdd / Treats fed today: $treatsFed")

        // Calculate hunger level and happiness
        val hungerLevel = if (catStatus.lastFed == null)
            100 else calculateHungerLevel(catStatus.lastFed)

        val moods = MoodManager.fromMap(catStatus.happinessReasons)
        if (hungerLevel > 50) {
            moods.overrideMood(Moods.Hunger.toString(), -(hungerLevel / 10))
        } else {
            moods.overrideMood(Moods.Hunger.toString(), (hungerLevel / 10))
        }

        // Prepare updated cat status with all changes
        val updatedCatStatus = catStatus.copy(
            dailyTreatsUsed = updatedDailyTreatsUsed,  // Ensure daily treats used are updated correctly
            treats = updatedTreats,                    // Update available treats after feeding (if fed)
            hunger = hungerLevel,
            happiness = calculateHappiness(hungerLevel),
            happinessReasons = moods.toMap(),
            lastFed = if (treatsFed > 0) LocalDateTime.now() else catStatus.lastFed, // Update last fed only if treats were fed
            lastUpdate = LocalDateTime.now() // Always update the last update time
        )

        Log.d("MyPetWorker", "Updated cat status: $updatedCatStatus")

        // Step 4: Save the updated cat status
        saveCatStatus(context, updatedCatStatus)
    }

    fun calculateHungerLevel(lastFedDate: LocalDateTime): Int {
        val currentDate = LocalDateTime.now()
        val hoursSinceLastFed = ChronoUnit.HOURS.between(lastFedDate, currentDate)

        // Assuming the cat gets fully hungry in 24 hours
        val maxHungerHours = 24

        // Normalize the hours since last fed to a value between 1 and 100
        val hungerLevel = ((hoursSinceLastFed.toDouble() / maxHungerHours) * 100).toInt()

        // Ensure the hunger level is within the range of 1 to 100
        return hungerLevel.coerceIn(1, 100)
    }

    fun calculateHappiness(hungerLevel: Int): Int {
        // Example logic: happiest when hungerLevel is 50, less happy as it deviates
        Log.d("MyPetWorker", "calculateHappiness: $hungerLevel (${(hungerLevel - 100).absoluteValue})")
        return ((hungerLevel - 100).absoluteValue).coerceAtLeast(1)
    }
}

fun Context.scheduleMyPetWorker() {
    val workRequest = OneTimeWorkRequestBuilder<MyPetWorker>()
        .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
        .build()
    WorkManager.getInstance(this).enqueueUniqueWork("worker", ExistingWorkPolicy.REPLACE, workRequest)
}

fun Context.schedulePeriodicMyPetWorker() {
    val periodicWorkRequest = PeriodicWorkRequestBuilder<MyPetWorker>(Duration.ofMinutes(35))
        .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
        .build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork("worker", ExistingPeriodicWorkPolicy.UPDATE, periodicWorkRequest)
}
