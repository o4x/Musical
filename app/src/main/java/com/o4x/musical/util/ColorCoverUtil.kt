package com.o4x.musical.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.R
import com.o4x.musical.extensions.isDarkMode
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
        background: Drawable? = null,
        char0: Char? = null,
        char1: Char? = null): Bitmap {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate the layout into a view and configure it the way you like
        val view = RelativeLayout(context)
        inflater.inflate(R.layout.color_cover, view, true)

        // Get view's
        val container = view.findViewById(R.id.container) as FrameLayout
        val charOne = view.findViewById(R.id.char_one) as TextView
        val charTwo = view.findViewById(R.id.char_two) as TextView
        // Setup view's
        container.background = background
        val charSize =
            ViewUtil.convertPixelsToDp(max(width, height).toFloat(), context.resources)
        charOne.apply {
            x = charSize / -2
            y = charSize / -4
            rotation = 30f

            textSize = charSize
            text = char0.toString()
        }
        charTwo.apply {
            x = charSize
            y = charSize / 2
            rotation = 30f

            textSize = charSize
            text = char1.toString()
        }


        //Provide it with a layout params. It should necessarily be wrapping the
        //content as we not really going to have a parent for it.
        view.layoutParams =
            ViewGroup.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)

        //Pre-measure the view so that height and width don't remain null.
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        //Create the bitmap
        val bitmap = Bitmap.createBitmap(view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888)
        //Create a canvas with the specified bitmap to draw into
        val c = Canvas(bitmap)

        //Render this view (and all of its children) to the given Canvas
        view.draw(c)
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