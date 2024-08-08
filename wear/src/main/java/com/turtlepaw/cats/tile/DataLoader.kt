package com.turtlepaw.cats.tile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.wear.protolayout.ResourceBuilders.ImageResource
import coil.imageLoader
import com.turtlepaw.cats.database.AppDatabase
import com.turtlepaw.cats.presentation.isNetworkConnected
import com.turtlepaw.cats.presentation.pages.safelyFetchAsync
import com.turtlepaw.cats.utils.Animals
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.decodeByteArray
import com.turtlepaw.cats.utils.enumFromJSON

class DataLoader {
    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    suspend fun getImage(context: Context): ImageResource? {
        return getImageAsBitmap(context)?.let {
            bitmapToImageResource(it)
        }
    }

    suspend fun getImageAsBitmap(context: Context): Bitmap? {
        if(!isNetworkConnected(context)){
            val data = try {
                getOfflineImage(context)
            } catch(error: Exception){
                throw error
            }

            return byteArrayToBitmap(
                data
            )
        } else {
            val data = safelyFetchAsync(1, getImageTypes(context)).first()
            return context.imageLoader.loadImage(context, data.url, 500)
        }
    }

    suspend fun getOfflineImage(context: Context): ByteArray {
        val database = AppDatabase.getDatabase(context)
        val image = database.imageDao().getRandomImage()
        return decodeByteArray(image.value)
    }

    fun getImageTypes(context: Context): List<Animals> {
        val animalTypes = context.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        ).getString(
            Settings.ANIMALS.getKey(),
            Settings.ANIMALS.getDefault()
        )
        return enumFromJSON(animalTypes)
    }
}