package com.turtlepaw.cats.database

import android.content.Context
import android.graphics.Bitmap.CompressFormat
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import coil.imageLoader
import com.turtlepaw.cats.presentation.pages.fetchPhotos
import com.turtlepaw.cats.tile.loadImage
import com.turtlepaw.cats.utils.Animals
import com.turtlepaw.cats.utils.DOWNLOAD_LIMIT
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.encodeToBase64
import com.turtlepaw.cats.utils.enumFromJSON
import kotlin.math.min

@Database(entities = [Image::class, Favorite::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

suspend fun ImageDao.downloadImages(
    context: Context,
    onProgressUpdate: suspend (Int, Int) -> Unit
) {
    val images = mutableListOf<Image>()
    val limit = DOWNLOAD_LIMIT
    val batchSizeLimit = 10 // Maximum batch size limit

    val animalTypes = context.getSharedPreferences(
        SettingsBasics.SHARED_PREFERENCES.getKey(),
        SettingsBasics.SHARED_PREFERENCES.getMode()
    ).getString(
        Settings.ANIMALS.getKey(),
        Settings.ANIMALS.getDefault()
    )
    val types = enumFromJSON(animalTypes)

    var imagesDownloaded = 0 // Initialize imagesDownloaded here

    val typeLimits = mutableMapOf<Animals, Int>()
    types.forEach { type ->
        // Set maximum limit for each animal type
        typeLimits[type] = if (type == Animals.BUNNIES) 1 else limit / types.size
    }

    while (imagesDownloaded < limit) {
        types.forEach { type ->
            val remainingLimit = limit - imagesDownloaded
            val count = min(min(typeLimits[type]!!, remainingLimit), batchSizeLimit)
            val photos = fetchPhotos(count, listOf(type))

            photos.forEach { photo ->
                val imageData = context.imageLoader.loadImage(context, photo.url, 500)
                if (imageData != null) {
                    images.add(
                        Image(
                            value = encodeToBase64(imageData, CompressFormat.WEBP_LOSSLESS, 80),
                            animal = type
                        )
                    )
                    imagesDownloaded++
                    onProgressUpdate(imagesDownloaded, limit)
                }
            }
        }
    }

    this.replaceImages(
        images
    )

    Log.d("Worker", "Done at $imagesDownloaded")
}

suspend fun downloadImage(
    url: String,
    context: Context
): String? {
    val imageData = context.imageLoader.loadImage(context, url, 500)
    return if(imageData != null){
        encodeToBase64(
            imageData,
            CompressFormat.WEBP_LOSSLESS,
            80
        )
    } else null
}
