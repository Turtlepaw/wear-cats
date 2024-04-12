package com.turtlepaw.cats.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
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

fun encodeToBase64(image: Bitmap, compressFormat: CompressFormat?, quality: Int): String {
    val byteArrayOS = ByteArrayOutputStream()
    image.compress(compressFormat!!, quality, byteArrayOS)
    return Base64.getEncoder().encodeToString(byteArrayOS.toByteArray())
}

fun decodeBase64(input: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(input, 0, input.size)
}

fun decodeByteArray(input: String): ByteArray {
    return Base64.getDecoder().decode(input)
}

class ImageViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
    private object PreferencesKeys {
        val IMAGE_KEY = stringSetPreferencesKey("images")
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    suspend fun downloadImages(context: Context) {
        dataStore.edit { preferences ->
            val images = emptySet<String>().toMutableSet()
            val limit = 50
            val animalTypes = context.getSharedPreferences(
                SettingsBasics.SHARED_PREFERENCES.getKey(),
                SettingsBasics.SHARED_PREFERENCES.getMode()
            ).getString(
                Settings.ANIMALS.getKey(),
                Settings.ANIMALS.getDefault()
            )
            val types = enumFromJSON(animalTypes)
            List(limit / 10) {
                val photos = fetchPhotos(10, types)
                photos.forEach {
                    val imageData = context.imageLoader.loadImage(context, it.url, 500)
                    if (imageData != null) {
                        images.add(
                            encodeToBase64(imageData, CompressFormat.WEBP_LOSSLESS, 80)
                        )
                    }
                }
            }
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
