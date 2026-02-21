package github.o4x.musical.util

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * A toolbox to handle UI overlapping with transparent system bars (Edge-to-Edge).
 */
object ViewInsetsUtils {

    /**
     * Applies system bar insets (Status Bar and/or Navigation Bar) as PADDING to the view.
     * Use this for containers like ScrollViews, RecyclerViews, or root layouts.
     */
    fun View.applySystemBarsPadding(
        applyTop: Boolean = false,
        applyBottom: Boolean = false,
        applyLeft: Boolean = false,
        applyRight: Boolean = false
    ) {
        // Capture initial padding to prevent infinite growth if listener fires multiple times
        val initialPaddingTop = this.paddingTop
        val initialPaddingBottom = this.paddingBottom
        val initialPaddingLeft = this.paddingLeft
        val initialPaddingRight = this.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.updatePadding(
                top = if (applyTop) initialPaddingTop + insets.top else initialPaddingTop,
                bottom = if (applyBottom) initialPaddingBottom + insets.bottom else initialPaddingBottom,
                left = if (applyLeft) initialPaddingLeft + insets.left else initialPaddingLeft,
                right = if (applyRight) initialPaddingRight + insets.right else initialPaddingRight
            )

            // Return insets so children can also receive them
            windowInsets
        }
    }

    /**
     * Applies system bar insets as MARGIN to the view.
     * Use this for specific elements like Buttons or Cards that shouldn't stretch,
     * but need to be pushed away from the screen edges.
     */
    fun View.applySystemBarsMargin(
        applyTop: Boolean = false,
        applyBottom: Boolean = false,
        applyLeft: Boolean = false,
        applyRight: Boolean = false
    ) {
        val layoutParams = this.layoutParams as? ViewGroup.MarginLayoutParams ?: return

        // Capture initial margins
        val initialMarginTop = layoutParams.topMargin
        val initialMarginBottom = layoutParams.bottomMargin
        val initialMarginLeft = layoutParams.leftMargin
        val initialMarginRight = layoutParams.rightMargin

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = if (applyTop) initialMarginTop + insets.top else initialMarginTop
                bottomMargin = if (applyBottom) initialMarginBottom + insets.bottom else initialMarginBottom
                leftMargin = if (applyLeft) initialMarginLeft + insets.left else initialMarginLeft
                rightMargin = if (applyRight) initialMarginRight + insets.right else initialMarginRight
            }
            windowInsets
        }
    }

    /**
     * Pushes a view up when the software keyboard (IME) appears.
     * Great for chat inputs or login buttons at the bottom of the screen.
     */
    fun View.applyKeyboardInsets() {
        val initialPaddingBottom = this.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Calculate bottom overlap (keyboard size or navigation bar size)
            val bottomOverlap = maxOf(imeInsets.bottom, systemBars.bottom)

            view.updatePadding(bottom = initialPaddingBottom + bottomOverlap)
            windowInsets
        }
    }

    /**
     * Applies the height of the AppBar (ActionBar/Toolbar) as PADDING to the top of the view.
     *
     * @param includeStatusBar If true, adds the status bar inset as well. Use true when your
     * AppBar is transparent/overlaying and shifts down due to edge-to-edge.
     */
    fun View.applyAppBarPadding(includeStatusBar: Boolean = true) {
        val initialPaddingTop = this.paddingTop
        val actionBarSize = context.getActionBarSize()

        if (includeStatusBar) {
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(top = initialPaddingTop + actionBarSize + insets.top)
                windowInsets
            }
        } else {
            this.updatePadding(top = initialPaddingTop + actionBarSize)
        }
    }

    /**
     * Applies the height of the AppBar (ActionBar/Toolbar) as MARGIN to the top of the view.
     *
     * @param includeStatusBar If true, adds the status bar inset as well.
     */
    fun View.applyAppBarMargin(includeStatusBar: Boolean = true) {
        val layoutParams = this.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        val initialMarginTop = layoutParams.topMargin
        val actionBarSize = context.getActionBarSize()

        if (includeStatusBar) {
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = initialMarginTop + actionBarSize + insets.top
                }
                windowInsets
            }
        } else {
            this.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = initialMarginTop + actionBarSize
            }
        }
    }

    /**
     * Helper function to extract the standard actionBarSize from the current context theme.
     */
    private fun Context.getActionBarSize(): Int {
        val typedValue = TypedValue()
        return if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        } else {
            // Fallback to 0 if the theme doesn't define actionBarSize
            0
        }
    }
}
