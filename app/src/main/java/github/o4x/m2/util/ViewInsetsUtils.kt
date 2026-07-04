package github.o4x.m2.util

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import github.o4x.m2.R

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
        applyLeft: Boolean = true,
        applyRight: Boolean = true
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
        applyLeft: Boolean = true,
        applyRight: Boolean = true
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
     * Applies the mini player bar height plus the navigation bar inset as PADDING to the
     * bottom of a scrolling view, so its content can scroll clear of the overlaid mini
     * player. Combine with clipToPadding="false" on the scrolling view.
     */
    fun View.applyMiniPlayerPadding() {
        val initialPaddingBottom = this.paddingBottom
        val miniPlayerHeight = context.getMiniPlayerHeight()

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = initialPaddingBottom + miniPlayerHeight + insets.bottom)
            windowInsets
        }
    }

    /**
     * Applies the height of the AppBar (ActionBar/Toolbar) as PADDING to the top of the view.
     *
     * @param includeStatusBar If true, adds the status bar inset as well. Use true when your
     * AppBar is transparent/overlaying and shifts down due to edge-to-edge.
     * @param withMiniPlayer If true, also pads the bottom with the mini player height plus the
     * navigation bar inset (see [applyMiniPlayerPadding]). Both paddings must be applied from a
     * single insets listener, since a view can only hold one.
     * @param withNavBarInset If true, pads the bottom with the navigation bar inset. Implied by
     * [withMiniPlayer]; use alone on screens without a mini player (e.g. settings).
     */
    fun View.applyAppBarPadding(
        includeStatusBar: Boolean = true,
        extra: Int = 0,
        withMiniPlayer: Boolean = false,
        withNavBarInset: Boolean = withMiniPlayer
    ) {
        val initialPaddingTop = this.paddingTop
        val initialPaddingBottom = this.paddingBottom
        val actionBarSize = context.getActionBarSize() + extra
        val miniPlayerHeight = if (withMiniPlayer) context.getMiniPlayerHeight() else 0

        if (includeStatusBar || withNavBarInset) {
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(
                    top = initialPaddingTop + actionBarSize +
                            if (includeStatusBar) insets.top else 0,
                    bottom = initialPaddingBottom + miniPlayerHeight +
                            if (withNavBarInset) insets.bottom else 0
                )
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
     * Total height of the mini player bar (progress bar + controls row).
     */
    private fun Context.getMiniPlayerHeight(): Int =
        resources.getDimensionPixelSize(R.dimen.mini_player_height) +
                resources.getDimensionPixelSize(R.dimen.mini_player_progress_height)

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
