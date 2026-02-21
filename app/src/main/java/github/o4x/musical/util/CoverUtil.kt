package github.o4x.musical.util

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.drawable.toBitmap


object CoverUtil {

    @JvmStatic
    fun addGradientTo(src: Bitmap, fromMiddle: Boolean = true): Bitmap {
        val w: Int = src.width
        val h: Int = src.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        canvas.drawBitmap(src, 0f, 0f, null)

        val paint = Paint()
        val shader = LinearGradient(
            0f,
            if (fromMiddle) h / 2f else 0f,
            0f,
            h.toFloat(), Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        return result
    }

    @JvmStatic
    fun doubleGradient(color1: Int, color2: Int, width: Int, height: Int): Bitmap {
        val gd = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(color1.withAlpha(.6f), color2.withAlpha(.6f))
        )
        gd.cornerRadius = 0f

        return addGradientTo(gd.toBitmap(width, height), fromMiddle = false)
    }

}