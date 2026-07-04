package github.o4x.m2.util

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

        // Multi-stop gradient eased with smoothstep so the fade ramps in and
        // out gradually instead of the abrupt edges a plain linear alpha ramp
        // produces. More stops also eliminates visible banding.
        val steps = 24
        val positions = FloatArray(steps + 1)
        val colors = IntArray(steps + 1)
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val eased = t * t * (3f - 2f * t)
            val a = ((1f - eased) * 255f).toInt().coerceIn(0, 255)
            positions[i] = t
            colors[i] = Color.argb(a, a, a, a)
        }

        val paint = Paint()
        paint.shader = LinearGradient(
            0f,
            if (fromMiddle) h / 2f else 0f,
            0f,
            h.toFloat(), colors, positions, Shader.TileMode.CLAMP
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        return result
    }

    @JvmStatic
    fun overlayColor(src: Bitmap, color: Int): Bitmap {
        val result = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0f, 0f, result.width.toFloat(), result.height.toFloat(), paint)
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