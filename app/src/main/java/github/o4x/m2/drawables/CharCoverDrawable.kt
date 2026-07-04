package github.o4x.m2.drawables

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import github.o4x.m2.imageloader.model.AudioFileCover
import github.o4x.m2.imageloader.model.MultiImage
import github.o4x.m2.model.Song
import github.o4x.m2.prefs.PreferenceUtil.isDarkMode
import github.o4x.m2.util.ColorUtil
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class CharCoverDrawable(private val coverData: CoverData, private val isClean: Boolean = false) : Drawable() {

    companion object {
        private const val TAG = "CharCoverDrawable"

        private val COLORS = listOf(
            arrayOf("#343d46", "#343d46"),
            arrayOf("#4f5b66", "#4f5b66"),
            arrayOf("#bf616a", "#bf616a"),
            arrayOf("#a3be8c", "#a3be8c"),
            arrayOf("#ebcb8b", "#ebcb8b"),
            arrayOf("#8fa1b3", "#8fa1b3"),
            arrayOf("#b48ead", "#b48ead"),
            arrayOf("#96b5b4", "#96b5b4"),
            arrayOf("#65737e", "#65737e"),
            arrayOf("#a7adba", "#a7adba"),
        ).map {
            it.map { it -> Color.parseColor(it)
            }.toIntArray()
        }

        private val COLORS_DARK = listOf(
            arrayOf("#343d46", "#343d46"),
            arrayOf("#4f5b66", "#4f5b66"),
            arrayOf("#bf616a", "#bf616a"),
            arrayOf("#a3be8c", "#a3be8c"),
            arrayOf("#ebcb8b", "#ebcb8b"),
            arrayOf("#8fa1b3", "#8fa1b3"),
            arrayOf("#b48ead", "#b48ead"),
            arrayOf("#96b5b4", "#96b5b4"),
            arrayOf("#65737e", "#65737e"),
            arrayOf("#a7adba", "#a7adba"),
            ).map {
            it.map { it -> Color.parseColor(it)
            }.toIntArray()
        }

        fun empty(): CharCoverDrawable {
            return CharCoverDrawable(CoverData(0, ""), true)
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val char0 = coverData.text.split(" ").firstOrNull()?.firstOrNull()
        .toString().uppercase(Locale.ROOT)
    private val char1 = coverData.text.split(" ").lastOrNull()?.firstOrNull()
        .toString().uppercase(Locale.ROOT)

    // Gradient and glyph paths are rebuilt only when the size or theme changes;
    // building them on every draw() made list scrolling visibly more expensive.
    private var cachedWidth = -1
    private var cachedHeight = -1
    private var cachedDarkMode = false
    private var gradient: GradientDrawable? = null
    private val path0 = Path()
    private val path1 = Path()

    fun setBlur(radius: Float): CharCoverDrawable {
        paint.maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        invalidateSelf()
        return this
    }

    override fun draw(canvas: Canvas) {
        ensureCache(canvas.width, canvas.height, isDarkMode)
        if (!isClean)
            gradient?.draw(canvas)
        canvas.save()
        canvas.rotate(30f)
        canvas.drawPath(path0, paint)
        canvas.drawPath(path1, paint)
        canvas.restore()
    }

    private fun ensureCache(width: Int, height: Int, darkMode: Boolean) {
        if (width == cachedWidth && height == cachedHeight && darkMode == cachedDarkMode) return
        cachedWidth = width
        cachedHeight = height
        cachedDarkMode = darkMode

        if (!isClean) {
            val id = coverData.id.toInt()
            val palette = if (darkMode) COLORS_DARK else COLORS
            gradient = GradientDrawable(
                GradientDrawable.Orientation.values()[
                        abs(id % GradientDrawable.Orientation.values().size)
                ], palette[abs(id % palette.size)]
            ).apply {
                cornerRadius = 0f
                setBounds(0, 0, width, height)
            }
        }

        paint.color = ColorUtil.withAlpha(
            if (darkMode) Color.WHITE else Color.BLACK, .1f)
        paint.textSize = max(width, height) * 1.4f // Text Size
        paint.isFakeBoldText = true

        paint.getTextPath(char0, 0, 1, width / -12f, height / 1.3f, path0)
        path0.close()
        paint.getTextPath(char1, 0, 1, width / 2f, height / 1.3f, path1)
        path1.close()
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