package com.o4x.appthemehelper.util

import android.content.Context
import androidx.annotation.ColorInt
import com.o4x.appthemehelper.ThemeStore
import androidx.appcompat.widget.Toolbar
import android.app.Activity

const val KEY_TOOLBAR_TINT_AUTO = "toolbar_tint_auto"
const val KEY_TOOLBAR_TEXT_COLOR = "toolbar_text_color"

@ColorInt
fun toolbarTextColor(
    context: Context,
    @ColorInt color: Int
): Int {
    return if (ThemeStore.prefs(context).getBoolean(KEY_TOOLBAR_TINT_AUTO, true)) {
        if (ColorUtil.isColorLight(color)) {
            ThemeStore.prefs(context).getInt(KEY_TOOLBAR_TEXT_COLOR, 0)
        } else {
            ThemeStore.prefs(context).getInt(KEY_TOOLBAR_TEXT_COLOR, 0)
        }
    } else {
        ThemeStore.prefs(context).getInt(KEY_TOOLBAR_TEXT_COLOR, 0)
    }
}

@ColorInt
fun toolbarTitleColor(
    context: Context,
    @ColorInt color: Int
): Int {
    return toolbarTextColor(context, color)
}

@ColorInt
fun toolbarSubtitleColor(
    context: Context,
    @ColorInt color: Int
): Int {
    return ColorUtil.adjustAlpha(toolbarTextColor(context, color), 0.75f)
}

fun colorizeToolbar(toolbar: Toolbar, @ColorInt color: Int, activity: Activity) {
    // TODO: Implementation
}
