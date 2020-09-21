package com.o4x.musical.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.drawable.toBitmap
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.extensions.getBitmapDrawable
import com.o4x.musical.extensions.isDarkMode
import com.o4x.musical.extensions.textColorPrimary
import java.util.*
import kotlin.math.abs
import kotlin.math.max

object ColorCoverUtil {

    @JvmStatic
    fun createSquareCoverWithText(context: Context, text: String, id: Int, size: Int = 1000): Bitmap {
        return create(
            context,
            size,
            size,
            getGradient(
                context,
                id
            ),
            text[0], text[1]
        )
    }

    private fun create(
        context: Context,
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

    private fun getGradient(context: Context, id: Int): Drawable {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(-0x9e9d9f, -0xececed))
        gradient.cornerRadius = 0f

        if (!context.isDarkMode) {
            // use custom color for light theme
            val pos = (id) % COLORS.size
            gradient.colors = COLORS[abs(pos)]
        } else {
            val pos = (id) % DESATURATED_COLORS.size
            gradient.colors = DESATURATED_COLORS[abs(pos)]
        }
        return gradient
    }

    private val COLORS = listOf(
        intArrayOf(0xff_00_c9_ff.toInt(), 0xff_92_fe_9d.toInt()),
        intArrayOf(0xff_f5_4e_a2.toInt(), 0xff_ff_76_76.toInt()),
        intArrayOf(0xff_17_ea_d9.toInt(), 0xff_92_fe_9d.toInt()),
        intArrayOf(0xff_7b_43_97.toInt(), 0xff_dc_24_30.toInt()),
        intArrayOf(0xff_1c_d8_d2.toInt(), 0xff_93_ed_c7.toInt()),
        intArrayOf(0xff_1f_86_ef.toInt(), 0xff_56_41_db.toInt()),
        intArrayOf(0xff_f0_2f_c2.toInt(), 0xff_60_94_ea.toInt()),
        intArrayOf(0xff_00_d2_ff.toInt(), 0xff_3a_7b_d5.toInt()),
        intArrayOf(0xff_f8_57_a6.toInt(), 0xff_ff_58_58.toInt()),
        intArrayOf(0xff_aa_ff_a9.toInt(), 0xff_11_ff_bd.toInt()),
        intArrayOf(0xff_00_c6_ff.toInt(), 0xff_00_72_ff.toInt()),
        intArrayOf(0xff_43_ce_a2.toInt(), 0xff_18_5a_9d.toInt()),
        intArrayOf(0xff_B6_50_DB.toInt(), 0xff_28_73_E1.toInt()),
        intArrayOf(0xff_17_ea_d9.toInt(), 0xff_60_98_ea.toInt()),
        intArrayOf(0xFF_38_ee_7e.toInt(), 0xFF_13_9c_8e.toInt()),
        intArrayOf(0xFF_38_ce_dc.toInt(), 0xFF_5a_89_e5.toInt()),
        intArrayOf(0xFF_15_85_cb.toInt(), 0xFF_2a_36_b3.toInt()),
        intArrayOf(0xFF_99_4f_bb.toInt(), 0xFF_30_34_b3.toInt()),
        intArrayOf(0xFF_83_00_ff.toInt(), 0xFF_dd_00_ff.toInt()),
        intArrayOf(0xFF_df_26_74.toInt(), 0xFF_fe_4f_32.toInt()),
        intArrayOf(0xFF_84_04_81.toInt(), 0xFF_e2_60_92.toInt()),
        intArrayOf(0xFF_ff_60_62.toInt(), 0xFF_ff_96_66.toInt()),
        intArrayOf(0xFF_fc_4e_1b.toInt(), 0xFF_f8_b3_33.toInt()),
        intArrayOf(0xFF_f7_9f_32.toInt(), 0xFF_fc_ca_1c.toInt())
    ).shuffled()

    private val DESATURATED_COLORS by lazy {
        COLORS.map { original ->
            val ints = original.copyOf()
            ints[0] = ColorUtil.desaturateColor(ints[1], .25f)
            ints[1] = ints[0]
            ints
        }
    }
}