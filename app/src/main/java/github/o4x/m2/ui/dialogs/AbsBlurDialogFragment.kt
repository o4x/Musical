package github.o4x.m2.ui.dialogs

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import java.util.function.Consumer

/**
 * Base class for the app's dialogs. On Android 12+ the dialog surface gets the same
 * frosted-glass treatment as the mini player and app bars: the panel turns translucent
 * and the window blurs whatever is behind it. The rounded panel drawable is moved from
 * the DialogLayout to the window background so the blur is clipped to its corners.
 * Where cross-window blur is unavailable (older devices, battery saver, accessibility)
 * the panel stays opaque.
 */
abstract class AbsBlurDialogFragment : DialogFragment() {

    private var panelDrawable: GradientDrawable? = null
    private var blurEnabledListener: Consumer<Boolean>? = null

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dialog?.window?.setupFrostedSurface()
        }
    }

    override fun onStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurEnabledListener?.let { dialog?.window?.windowManager?.removeCrossWindowBlurEnabledListener(it) }
            blurEnabledListener = null
        }
        super.onStop()
    }

    override fun onDestroyView() {
        panelDrawable = null
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun Window.setupFrostedSurface() {
        if (panelDrawable == null) {
            val dialogLayout = (dialog as? MaterialDialog)?.view ?: return
            val panel = dialogLayout.background as? GradientDrawable ?: return
            dialogLayout.background = null
            setBackgroundDrawable(panel)
            panelDrawable = panel
        }
        val panel = panelDrawable ?: return
        val blurRadius = (BLUR_RADIUS_DP * resources.displayMetrics.density).toInt()
        val listener = Consumer<Boolean> { enabled ->
            panel.alpha = if (enabled) FROSTED_PANEL_ALPHA else 0xFF
            setBackgroundBlurRadius(if (enabled) blurRadius else 0)
        }
        blurEnabledListener = listener
        windowManager.addCrossWindowBlurEnabledListener(listener)
    }

    companion object {
        private const val BLUR_RADIUS_DP = 20f

        /** Same 70% scrim alpha as the blurred mini player / app bar backgrounds. */
        private const val FROSTED_PANEL_ALPHA = 0xB3
    }
}
