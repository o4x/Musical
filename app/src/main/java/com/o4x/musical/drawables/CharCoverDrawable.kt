package com.o4x.musical.drawables

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
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

            arrayOf("#E9E9E9", "#F6F6F6"),
        ).map {
            it.map { it -> Color.parseColor(it)
            }.toIntArray()
        }

        private val COLORS_DARK = listOf(
            arrayOf("#202020", "#202020"),
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
                if (isDarkMode) Color.WHITE else Color.BLACK,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && canvas.isHardwareAccelerated) 0.4f else 0.1f) // Text Color
        // Because OVERLAY work from API 28

        paint.textSize = max(canvas.width, canvas.height) * 1.4f // Text Size
        paint.isFakeBoldText = true
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY) // Text Overlapping Pattern

        canvas.rotate(30f)

        val char0 = text.split(" ").firstOrNull()?.firstOrNull()
            .toString().toUpperCase(Locale.ROOT)
        val char1 = text.split(" ").lastOrNull()?.firstOrNull()
            .toString().toUpperCase(Locale.ROOT)

        val path0 = Path()
        paint.getTextPath(char0, 0, 1, canvas.width / -12f, canvas.height / 1.3f, path0)
        path0.close()
        canvas.drawPath(path0, paint)

        val path1 = Path()
        paint.getTextPath(char1, 0, 1, canvas.width / 2f, canvas.height / 1.3f, path1)
        path1.close()
        canvas.drawPath(path1, paint)
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