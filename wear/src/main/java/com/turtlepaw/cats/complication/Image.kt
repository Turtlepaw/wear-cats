package com.turtlepaw.cats.complication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader

fun applyVignetteEffect(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val radius = width.coerceAtLeast(height) / 1.5f

    val vignetteBitmap = Bitmap.createBitmap(width, height, bitmap.config)
    val canvas = Canvas(vignetteBitmap)
    canvas.drawBitmap(bitmap, 0f, 0f, null)

    val paint = Paint()
    val gradient = RadialGradient(
        (width / 2).toFloat(),
        (height / 2).toFloat(),
        radius,
        intArrayOf(0x00000000, 0x7f000000),
        floatArrayOf(0.0f, 1.0f),
        Shader.TileMode.CLAMP
    )
    paint.shader = gradient
    paint.isDither = true

    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    return vignetteBitmap
}

fun getRoundedCroppedBitmap(bitmap: Bitmap): Bitmap {
    val size = bitmap.width.coerceAtMost(bitmap.height)
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint()
    val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())

    val path = Path()
    path.addRoundRect(rect, size / 2f, size / 2f, Path.Direction.CCW)
    canvas.clipPath(path)

    // Calculate the top and left offsets to center the square crop
    val left = (bitmap.width - size) / 2
    val top = (bitmap.height - size) / 2

    // Define the source rectangle (the area of the original bitmap to draw)
    val srcRect = Rect(left, top, left + size, top + size)

    // Draw the central square portion of the original bitmap
    canvas.drawBitmap(bitmap, srcRect, rect, paint)

    return output
}

fun getCroppedSquareBitmap(bitmap: Bitmap): Bitmap {
    val size = bitmap.width.coerceAtMost(bitmap.height)
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint()

    // Calculate the top and left offsets to center the square crop
    val left = (bitmap.width - size) / 2
    val top = (bitmap.height - size) / 2

    // Define the source rectangle (the area of the original bitmap to draw)
    val srcRect = Rect(left, top, left + size, top + size)

    // Define the destination rectangle (the area to draw the cropped bitmap)
    val destRect = Rect(0, 0, size, size)

    // Draw the central square portion of the original bitmap
    canvas.drawBitmap(bitmap, srcRect, destRect, paint)

    return output
}
