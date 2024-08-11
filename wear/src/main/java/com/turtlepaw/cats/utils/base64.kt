package com.turtlepaw.cats.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

fun decodeBitmapFromRawResource(context: Context, resourceId: Int): Bitmap? {
    return try {
        // Open the raw resource file
        val inputStream = context.resources.openRawResource(resourceId)
        val base64String = inputStream.bufferedReader().use { it.readText() }

        // Decode the Base64 string to a byte array
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

        // Decode the byte array to a Bitmap
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}