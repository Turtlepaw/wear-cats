package com.turtlepaw.cats.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.imageLoader
import com.turtlepaw.cats.presentation.pages.fetchPhotos
import com.turtlepaw.cats.tile.loadImage
import com.turtlepaw.cats.utils.ImageViewModel.PreferencesKeys.IMAGE_KEY
import kotlinx.coroutines.flow.first
import java.io.ByteArrayOutputStream
import java.util.Base64
import kotlin.math.min

fun encodeToBase64(image: Bitmap, compressFormat: CompressFormat?, quality: Int): String {
    val byteArrayOS = ByteArrayOutputStream()
    image.compress(compressFormat!!, quality, byteArrayOS)
    return Base64.getEncoder().encodeToString(byteArrayOS.toByteArray())
}

fun encodeToBase64(image: ByteArray): String {
    return Base64.getEncoder().encodeToString(image)
}

fun decodeBase64(input: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(input, 0, input.size)
}

fun decodeByteArray(input: String): ByteArray {
    return Base64.getDecoder().decode(input)
}

val DOWNLOAD_LIMIT = 150

@Deprecated(
    message = "Deprecated in favor of Room, use AppDatabase#download()"
)
class ImageViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
    private object PreferencesKeys {
        val IMAGE_KEY = stringSetPreferencesKey("images")
    }

    @Deprecated(
        message = "Deprecated in favor of Room, use AppDatabase#download()"
    )
    suspend fun downloadImages(
        context: Context,
        onProgressUpdate: suspend (Int, Int) -> Unit
    ) {
        dataStore.edit { preferences ->
            val images = mutableSetOf<String>()
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
                                encodeToBase64(imageData, CompressFormat.WEBP_LOSSLESS, 80)
                            )
                            imagesDownloaded++
                            onProgressUpdate(imagesDownloaded, limit)
                        }
                    }
                }
            }

            Log.d("Worker", "Done at $imagesDownloaded")
            preferences[IMAGE_KEY] = images
        }
    }

    suspend fun getImages(): List<String> {
        val preferences = dataStore.data.first() // blocking call to get the latest preferences
        return preferences[IMAGE_KEY]?.toList() ?: emptyList()
    }

    companion object {
        private lateinit var instance: ImageViewModel

        fun getInstance(dataStore: DataStore<Preferences>): ImageViewModel {
            if (!::instance.isInitialized) {
                instance = ImageViewModel(dataStore)
            }
            return instance
        }
    }
}

class ImageViewModelFactory(private val dataStore: DataStore<Preferences>) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImageViewModel(dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
