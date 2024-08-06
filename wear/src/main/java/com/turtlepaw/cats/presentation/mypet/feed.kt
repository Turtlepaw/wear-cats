import android.content.Context
import android.util.Log
import com.turtlepaw.cats.mypet.CatStatus
import com.turtlepaw.cats.mypet.getCatStatusFlow
import com.turtlepaw.cats.mypet.saveCatStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

suspend fun feedCatTreat(context: Context, treatsUsed: Int) {
    // Fetch the current cat status
    val catStatus = getCatStatusFlow(context).firstOrNull() ?: CatStatus(
        hunger = 0,
        treats = 0,
        happinessReasons = mapOf(),
        happiness = 0
    )

    // Get the total number of treats available
    val totalTreats = catStatus.treats

    // Calculate how much hunger will decrease based on the number of treats
    val hungerDecreasePerTreat = 10 // Example: each treat decreases hunger by 10
    val totalHungerDecrease = (totalTreats * hungerDecreasePerTreat).coerceAtMost(catStatus.hunger)

    // Calculate new hunger and happiness
    val updatedHunger = (catStatus.hunger - totalHungerDecrease).coerceAtLeast(0)
    val updatedHappiness = (catStatus.happiness + totalTreats * 2).coerceAtMost(100) // Example: increase happiness by 2 per treat

    // Update the cat's status
    val updatedCatStatus = catStatus.copy(
        treats = 0, // All treats are used
        hunger = updatedHunger,
        happiness = updatedHappiness,
        happinessReasons = mapOf("Feed All" to totalTreats)
    )

    Log.d("MyPetWorker", "Updated cat status after feeding all treats: $updatedCatStatus")

    // Save the updated cat status
    saveCatStatus(context, updatedCatStatus)
}
