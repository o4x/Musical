package github.o4x.musical.util

import android.R
import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.PathInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

object ViewUtil {
    const val PHONOGRAPH_ANIM_TIME: Int = 400

    fun createBackgroundColorTransition(
        v: View?,
        @ColorInt startColor: Int,
        @ColorInt endColor: Int
    ): Animator {
        return createColorAnimator(v, "backgroundColor", startColor, endColor)
    }

    fun createTextColorTransition(
        v: TextView?,
        @ColorInt startColor: Int,
        @ColorInt endColor: Int
    ): Animator {
        return createColorAnimator(v, "textColor", startColor, endColor)
    }

    private fun createColorAnimator(
        target: Any?,
        propertyName: String?,
        @ColorInt startColor: Int,
        @ColorInt endColor: Int
    ): Animator {
        val animator: ObjectAnimator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator = ObjectAnimator.ofArgb(target, propertyName, startColor, endColor)
        } else {
            animator = ObjectAnimator.ofInt(target, propertyName, startColor, endColor)
            animator.setEvaluator(ArgbEvaluator())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator.setInterpolator(PathInterpolator(0.4f, 0f, 1f, 1f))
        }
        animator.setDuration(PHONOGRAPH_ANIM_TIME.toLong())
        return animator
    }

    fun createSelectorDrawable(context: Context?, @ColorInt color: Int): Drawable {
        val baseSelector = StateListDrawable()
        baseSelector.addState(intArrayOf(R.attr.state_activated), ColorDrawable(color))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return RippleDrawable(
                ColorStateList.valueOf(color),
                baseSelector,
                ColorDrawable(Color.WHITE)
            )
        }

        baseSelector.addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
        baseSelector.addState(intArrayOf(R.attr.state_pressed), ColorDrawable(color))
        return baseSelector
    }

    @JvmStatic
    fun hitTest(v: View, parent: View, x: Int, y: Int): Boolean {
        val viewLocation = IntArray(2)
        v.getLocationInWindow(viewLocation)

        val rootLocation = IntArray(2)
        parent.getLocationInWindow(rootLocation)

        val vx = viewLocation[0] - rootLocation[0]
        val vy = viewLocation[1] - rootLocation[1]
        val width = v.getWidth()
        val height = v.getHeight()

        return (x >= vx) && (x <= vx + width) && (y >= vy) && (y <= vy + height)
    }

    fun setUpFastScrollRecyclerViewColor(
        context: Context,
        recyclerView: FastScrollRecyclerView,
        themeColor: Int
    ) {
        recyclerView.setPopupBgColor(themeColor)
        recyclerView.setPopupTextColor(context.textColorPrimary())
        recyclerView.setThumbColor(themeColor)
        recyclerView.setTrackColor(context.colorControlNormal().withAlpha(.12f))
    }

    fun convertDpToPixel(dp: Float, resources: Resources): Float {
        val metrics = resources.getDisplayMetrics()
        return dp * metrics.density
    }

    fun convertPixelsToDp(px: Float, resources: Resources): Float {
        val metrics = resources.getDisplayMetrics()
        return px / metrics.density
    }

    fun getViewBackgroundColor(view: View): Int {
        var color = Color.TRANSPARENT
        val background = view.getBackground()
        if (background is ColorDrawable) color = background.getColor()

        return color
    }

    fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap =
            Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.getBackground()
        if (bgDrawable != null) bgDrawable.draw(canvas)
        else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }

    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.getLayoutParams() is MarginLayoutParams) {
            val p = v.getLayoutParams() as MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }

    fun setScrollBarColor(scr: View?, @ColorInt color: Int) {
        try {
            val mScrollCacheField = View::class.java.getDeclaredField("mScrollCache")
            mScrollCacheField.setAccessible(true)
            val mScrollCache = mScrollCacheField.get(scr) // scr is your Scroll View

            val scrollBarField = mScrollCache!!.javaClass.getDeclaredField("scrollBar")
            scrollBarField.setAccessible(true)
            val scrollBar = scrollBarField.get(mScrollCache)

            val method = scrollBar!!.javaClass.getDeclaredMethod(
                "setVerticalThumbDrawable",
                Drawable::class.java
            )
            method.setAccessible(true)

            // Set your drawable here.
            method.invoke(scrollBar, ColorDrawable(color))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
