package com.o4x.musical.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.extensions.isDarkMode
import com.o4x.musical.extensions.textColorPrimary
import com.o4x.musical.imageloader.model.CoverData
import com.o4x.musical.util.CoverUtil
import com.o4x.musical.util.Util
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class SquareImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var isImageSet: Boolean = false

    var coverData: CoverData? = null
        set(value) {
            field = value
            invalidate()
        }


    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        isImageSet = drawable != null
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        isImageSet = bm != null
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        if (!isImageSet && canvas != null && coverData != null) {
            drawGradient(context, canvas, coverData!!.id.toInt())
            drawChars(context, canvas, coverData!!.text)
        }
    }

    companion object {

        private fun drawGradient(context: Context, canvas: Canvas, id: Int) {
            val colors = if (context.isDarkMode) {
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

        private fun drawChars(context: Context, canvas: Canvas, text: String) {
            // Render char's
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color =
                ColorUtil.withAlpha(context.textColorPrimary(), 0.2f) // Text Color
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

        // this is use cpu for render so not good
        @JvmStatic
        fun createSquareCoverWithText(
            context: Context,
            text: String,
            id: Long,
            size: Int = DEFAULT_SIZE
        ): Bitmap {
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            drawGradient(context, canvas, id.toInt())
            drawChars(context, canvas, text)

            return bmp
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
            it.map { it -> Color.parseColor(it)
            }.toIntArray()
        }

        private val COLORS_DARK = listOf(
            arrayOf("#2E2E2E", "#373737"),
        ).map {
            it.map { it -> Color.parseColor(it)
            }.toIntArray()
        }

        @JvmField
        val DEFAULT_SIZE = Util.getScreenWidth() / 4
    }
}