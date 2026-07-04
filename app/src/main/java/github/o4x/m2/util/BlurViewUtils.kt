package github.o4x.m2.util

import android.view.Window
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView

/**
 * Shared setup for the frosted-glass chrome (mini player, app bars) drawn with BlurView.
 */
object BlurViewUtils {

    private const val BLUR_RADIUS = 20f

    /**
     * Blurs [target]'s content behind this view. The translucent scrim on top comes from
     * the app:blurOverlayColor attribute (or [BlurView.setOverlayColor]).
     */
    fun BlurView.setupBlur(window: Window, target: BlurTarget) {
        setupWith(target)
            .setFrameClearDrawable(window.decorView.background)
            .setBlurRadius(BLUR_RADIUS)
    }
}
