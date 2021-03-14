package com.o4x.musical.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.o4x.musical.R

fun Context.scaledDrawableResources(
    @DrawableRes id: Int,
    @DimenRes width: Int,
    @DimenRes height: Int
): Drawable {
    val w = resources.getDimension(width).toInt()
    val h = resources.getDimension(height).toInt()
    return scaledDrawable(id, w, h)
}

fun Context.scaledDrawable(@DrawableRes id: Int, width: Int, height: Int): Drawable {
    val bmp = BitmapFactory.decodeResource(resources, id)
    val bmpScaled = Bitmap.createScaledBitmap(bmp, width, height, false)
    return BitmapDrawable(resources, bmpScaled)
}

fun Drawable.getBitmapDrawable(): Bitmap {
    val bmp = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    draw(canvas)
    return bmp
}

fun getAdaptiveIconDrawable(context: Context): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        AdaptiveIconDrawable(
            ContextCompat.getDrawable(context, R.drawable.ic_launcher_background),
            ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)
        )
    } else {
        ContextCompat.getDrawable(context, R.drawable.color_circle_gradient)!!
    }
}