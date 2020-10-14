package com.o4x.musical.util

import android.R.attr.src
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.drawable.toBitmap
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.extensions.isDarkMode
import com.o4x.musical.extensions.textColorPrimary
import java.util.*
import kotlin.math.abs
import kotlin.math.max


class CoverUtil constructor(val context: Context) {

    private fun create(
        width: Int,
        height: Int,
        background: Drawable,
        char0: Char? = null,
        char1: Char? = null,
    ): Bitmap {

        val charSize = max(width, height) * 1.4f

        //Create the bitmap
        val bitmap = background.toBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Create a canvas with the specified bitmap to draw into
        val c = Canvas(bitmap)


        // Render char's
        val paint = Paint()
        paint.color = ColorUtil.withAlpha(context.textColorPrimary(), 0.5f)// Text Color
        paint.textSize = charSize // Text Size
        paint.isFakeBoldText = true
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY) // Text Overlapping Pattern

        c.rotate(30f)

        c.drawText(
            char0.toString().toUpperCase(Locale.ROOT),
            width / -12f,
            height / 1.3f,
            paint)

        c.drawText(
            char1.toString().toUpperCase(Locale.ROOT),
            width / 2f,
            height / 1.3f,
            paint)

        return bitmap
    }

    private fun getGradient(id: Int): Drawable {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.values()[
                    abs((id) % GradientDrawable.Orientation.values().size)
            ]
            , intArrayOf(-0x9e9d9f, -0xececed))
        gradient.cornerRadius = 0f

        if (context.isDarkMode) {
            val pos = (id) % COLORS_DARK.size
            gradient.colors = COLORS_DARK[abs(pos)]
        } else {
            // use custom color for light theme
            val pos = (id) % COLORS.size
            gradient.colors = COLORS[abs(pos)]
        }
        return gradient
    }

    companion object {
        const val DEFAULT_SIZE = 500

        @JvmStatic
        fun createSquareCoverWithText(
            context: Context,
            text: String,
            id: Long,
            size: Int = DEFAULT_SIZE
        )
                = createSquareCoverWithText(context, text, id.toInt(), size)


        @JvmStatic
        fun createSquareCoverWithText(
            context: Context,
            text: String,
            id: Int,
            size: Int = DEFAULT_SIZE
        ): Bitmap {
            val coverUtil = CoverUtil(context)
            return coverUtil.create(
                size,
                size,
                coverUtil.getGradient(
                    id
                ),
                text.split(" ").firstOrNull()?.firstOrNull(),
                text.split(" ").lastOrNull()?.firstOrNull()
            )
        }

        private val COLORS = listOf(
            arrayOf("#8693AB", "#BDD4E7"),
            arrayOf("#F5E3E6", "#D9E4F5"),
            arrayOf("#BBDBBE", "#DEEBDD"),
            arrayOf("#E9E9E9", "#F6F6F6"),
            arrayOf("#FFB4A2", "#FFCDB2"),
            arrayOf("#EDF1F4", "#C3CBDC"),
            arrayOf("#F7D4D4", "#F6ECC4"),
            ).map {
            it.map {
                it -> Color.parseColor(it)
            }.toIntArray()
        }

        private val COLORS_DARK = listOf(
            arrayOf("#2E2E2E", "#373737"),
        ).map {
            it.map {
                    it -> Color.parseColor(it)
            }.toIntArray()
        }

        @JvmStatic
        fun addGradientTo(src: Bitmap): Bitmap {
            val w: Int = src.width
            val h: Int = src.height
            val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)

            canvas.drawBitmap(src, 0f, 0f, null)

            val paint = Paint()
            val shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(), Color.WHITE, Color.TRANSPARENT, Shader.TileMode.MIRROR)
            paint.shader = shader
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

            return result
        }
    }
}