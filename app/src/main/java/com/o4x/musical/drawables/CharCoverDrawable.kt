package com.o4x.musical.drawables

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.model.MultiImage
import com.o4x.musical.model.Song
import com.o4x.musical.prefs.PreferenceUtil.isDarkMode
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class CharCoverDrawable(private val coverData: CoverData) : Drawable() {

    companion object {
        private const val TAG = "CharCoverDrawable"

        private val COLORS = listOf(
            arrayOf("#8693AB", "#BDD4E7"),
            arrayOf("#F5E3E6", "#D9E4F5"),
            arrayOf("#BBDBBE", "#DEEBDD"),
            arrayOf("#E9E9E9", "#F6F6F6"),
            arrayOf("#FFB4A2", "#FFCDB2"),
            arrayOf("#EDF1F4", "#C3CBDC"),
            arrayOf("#F7D4D4", "#F6ECC4"),
        ).map {
            it.map { it -> Color.parseColor(it)
            }.toIntArray()
        }

        private val COLORS_DARK = listOf(
            arrayOf("#2E2E2E", "#373737"),
        ).map {
            it.map { it -> Color.parseColor(it)
            }.toIntArray()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setBlur(radius: Float): CharCoverDrawable {
        paint.maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        invalidateSelf()
        return this
    }

    override fun draw(canvas: Canvas) {
        drawGradient(canvas, coverData.id.toInt())
        drawChars(canvas, coverData.text)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    private fun drawGradient(canvas: Canvas, id: Int) {
        val colors = if (isDarkMode) {
            val pos = (id) % COLORS_DARK.size
            COLORS_DARK[abs(pos)]
        } else {
            // use custom color for light theme
            val pos = (id) % COLORS.size
            COLORS[abs(pos)]
        }

        val gradient = GradientDrawable(
            GradientDrawable.Orientation.values()[
                    abs((id) % GradientDrawable.Orientation.values().size)
            ], colors
        )
        gradient.cornerRadius = 0f

        gradient.setBounds(0, 0, canvas.width, canvas.height)
        gradient.draw(canvas)
    }

    private fun drawChars(canvas: Canvas, text: String) {
        // Render char's
        paint.color =
            ColorUtil.withAlpha(
                if (isDarkMode) Color.WHITE else Color.BLACK, 0.2f) // Text Color

        paint.textSize = max(canvas.width, canvas.height) * 1.4f // Text Size
        paint.isFakeBoldText = true
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY) // Text Overlapping Pattern

        canvas.rotate(30f)

        canvas.drawText(
            text.split(" ").firstOrNull()?.firstOrNull()
                .toString().toUpperCase(Locale.ROOT),
            canvas.width / -12f,
            canvas.height / 1.3f,
            paint
        )

        canvas.drawText(
            text.split(" ").lastOrNull()?.firstOrNull()
                .toString().toUpperCase(Locale.ROOT),
            canvas.width / 2f,
            canvas.height / 1.3f,
            paint
        )
    }
}

class CoverData(val id: Long, val text: String) {
    companion object {
        fun from(song: Song): CoverData {
            return CoverData(song.albumId, song.albumName)
        }

        fun from(audioFileCover: AudioFileCover): CoverData {
            return CoverData(audioFileCover.hashCode().toLong(), audioFileCover.title)
        }

        fun from(multiImage: MultiImage): CoverData {
            return CoverData(multiImage.id, multiImage.name)
        }

        fun from(url: String, name: String?): CoverData {
            return CoverData(url.hashCode().toLong(), name ?: "")
        }
    }
}