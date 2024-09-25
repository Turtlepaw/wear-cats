package com.turtlepaw.cats.services

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import android.content.Context
import androidx.work.WorkerParameters
import com.turtlepaw.cats.mypet.CatStatus
import com.turtlepaw.cats.mypet.getCatStatusFlow
import com.turtlepaw.cats.mypet.getStepGoalFlow
import kotlinx.coroutines.flow.firstOrNull

class MyPetWorkerTest {
    private lateinit var context: Context
    private lateinit var worker: MyPetWorker

    @Before
    fun setup() {
        // Mocking the Android Context and WorkerParameters
        context = mock(Context::class.java)
        val workerParams = mock(WorkerParameters::class.java)
        worker = MyPetWorker(context, workerParams)
    }

    @Test
    fun testTreatsCalculationWith50PercentSteps() = runBlocking {
        // Mock data: Set step goal to 10,000 and simulate 50% step progress
        val mockStepGoal = 10000
        val mockSteps = 5000L // 50% step completion

        `when`(getStepGoalFlow(context).firstOrNull()).thenReturn(mockStepGoal)
        `when`(getCatStatusFlow(context).firstOrNull()).thenReturn(
            CatStatus(
                hunger = 100,
                treats = 0,
                dailyTreatsUsed = 0,
                happinessReasons = mapOf(),
                happiness = 1,
                lastFed = null,
                lastUpdate = null
            )
        )

        // Call the function to test
        worker.updateCatStatus(context, mockSteps)

        // After feeding, 5 treats should be available (10 max, 50% step completion)
        val updatedCatStatus = getCatStatusFlow(context).firstOrNull()
        assertEquals(5, updatedCatStatus?.treats)  // Verify 5 treats are available
        assertEquals(0, updatedCatStatus?.dailyTreatsUsed)  // No treats used yet
    }
}
