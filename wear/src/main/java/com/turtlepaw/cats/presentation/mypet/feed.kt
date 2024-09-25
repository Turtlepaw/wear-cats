import android.content.Context
import android.util.Log
import com.turtlepaw.cats.mypet.CatStatus
import com.turtlepaw.cats.mypet.MoodManager
import com.turtlepaw.cats.mypet.Moods
import com.turtlepaw.cats.mypet.getCatStatusFlow
import com.turtlepaw.cats.mypet.saveCatStatus
import com.turtlepaw.cats.services.scheduleMyPetWorker
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDateTime

suspend fun feedCatTreat(context: Context, treatsUsed: Int) {
    // Fetch the current cat status
    val catStatus = getCatStatusFlow(context).firstOrNull() ?: CatStatus(
        hunger = 0,
        treats = 0,
        dailyTreatsUsed = 0,
        lastUpdate = null,
        happinessReasons = mapOf(),
        happiness = 0,
        lastFed = LocalDateTime.now()
    )

    // Update the cat's status
    val updatedCatStatus = catStatus.copy(
        treats = 0, // All treats are used
        dailyTreatsUsed = treatsUsed,
//        hunger = updatedHunger,
//        happiness = updatedHappiness,
        happinessReasons = MoodManager.fromMap(catStatus.happinessReasons)
            .overrideMood(
                Moods.Hunger.toString(),
                5
            )
            .toMap(),
        lastFed = LocalDateTime.now()
    )

    Log.d("MyPetWorker", "Updated cat status after feeding all treats: $updatedCatStatus")

    // Save the updated cat status
    saveCatStatus(context, updatedCatStatus)

    context.scheduleMyPetWorker()
}
