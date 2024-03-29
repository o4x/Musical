package com.o4x.appthemehelper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import com.o4x.appthemehelper.extensions.primaryColor
import com.o4x.appthemehelper.extensions.surfaceColor
import com.o4x.appthemehelper.util.ATHUtil
import com.o4x.appthemehelper.util.ColorUtil
import com.o4x.appthemehelper.util.TintHelper
import com.o4x.appthemehelper.util.ToolbarContentTintHelper

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
object ATH {

    @SuppressLint("CommitPrefEdits")
    fun didThemeValuesChange(context: Context, since: Long): Boolean {
        return ThemeStore.prefs(context).getLong(
            ThemeStorePrefKeys.VALUES_CHANGED,
            -1
        ) > since
    }

    fun setLightStatusbar(activity: Activity, enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = activity.window.decorView
            val systemUiVisibility = decorView.systemUiVisibility
            if (enabled) {
                decorView.systemUiVisibility =
                    systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility =
                    systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }

    fun setLightNavigationbar(activity: Activity, enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView = activity.window.decorView
            var systemUiVisibility = decorView.systemUiVisibility
            systemUiVisibility = if (enabled) {
                systemUiVisibility or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                systemUiVisibility and SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            decorView.systemUiVisibility = systemUiVisibility
        }
    }

    fun setLightNavigationbarAuto(activity: Activity, bgColor: Int) {
        setLightNavigationbar(activity, ColorUtil.isColorLight(bgColor))
    }

    fun setNavigationbarColorAuto(activity: Activity) {
        setNavigationbarColor(activity, activity.surfaceColor())
    }

    fun setNavigationbarColor(activity: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.window.navigationBarColor = color
        } else {
            activity.window.navigationBarColor = ColorUtil.darkenColor(color)
        }
        setLightNavigationbarAuto(activity, color)
    }

    fun setNavigationBarDividerColorAuto(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.window.navigationBarDividerColor = ColorUtil.blendColors(
                ATHUtil.resolveColor(activity, R.attr.elevationOverlayColor),
                activity.window.navigationBarColor,
                0.9f
            )
        }
    }

    fun setActivityToolbarColorAuto(activity: Activity, toolbar: Toolbar?) {
        setActivityToolbarColor(activity, toolbar, activity.surfaceColor())
    }

    fun setActivityToolbarColor(
        activity: Activity, toolbar: Toolbar?,
        color: Int
    ) {
        if (toolbar == null) {
            return
        }
        toolbar.setBackgroundColor(color)
        ToolbarContentTintHelper.setToolbarContentColorBasedOnToolbarColor(activity, toolbar, color)
    }

    fun setTaskDescriptionColorAuto(activity: Activity) {
        setTaskDescriptionColor(activity, activity.surfaceColor())
    }

    fun setTaskDescriptionColor(activity: Activity, @ColorInt color: Int) {
        var colorFinal = color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Task description requires fully opaque color
            colorFinal = ColorUtil.stripAlpha(colorFinal)
            // Sets color of entry in the system recents page
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activity.setTaskDescription(
                    ActivityManager.TaskDescription(
                        activity.title as String?,
                        -1,
                        colorFinal
                    )
                )
            } else {
                activity.setTaskDescription(ActivityManager.TaskDescription(activity.title as String?))
            }
        }
    }

    fun setTint(view: View, @ColorInt color: Int) {
        TintHelper.setTintAuto(view, color, false)
    }

    fun setBackgroundTint(view: View, @ColorInt color: Int) {
        TintHelper.setTintAuto(view, color, true)
    }
}