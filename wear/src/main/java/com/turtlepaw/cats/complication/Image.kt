package com.turtlepaw.cats.complication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
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
    canvas.drawBitmap(bitmap, null, rect, paint)

    return output
}
