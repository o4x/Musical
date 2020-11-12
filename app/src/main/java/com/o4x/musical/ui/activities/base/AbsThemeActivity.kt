package com.o4x.musical.ui.activities.base

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import code.name.monkey.appthemehelper.ATH
import code.name.monkey.appthemehelper.ATHActivity
import code.name.monkey.appthemehelper.common.ATHToolbarActivity
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.VersionUtils
import com.o4x.musical.LanguageContextWrapper
import com.o4x.musical.R
import com.o4x.musical.appshortcuts.DynamicShortcutManager
import com.o4x.musical.extensions.surfaceColor
import com.o4x.musical.util.PreferenceUtil
import com.o4x.musical.util.Util
import com.o4x.musical.util.theme.ThemeManager
import java.util.*

abstract class AbsThemeActivity : ATHActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        toggleScreenOn()
    }

    private fun setTheme() {
        setTheme(ThemeManager.getThemeResValue())
        setDefaultNightMode(ThemeManager.getNightMode())
        DynamicShortcutManager(this).updateDynamicShortcuts()
    }

    override fun updateTheme() {
        recreate()
    }

    private fun toggleScreenOn() {
        if (PreferenceUtil.isScreenOnEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun setDrawUnderBar() {
        Util.setAllowDrawUnderBar(window)
    }

    fun setDrawUnderStatusBar() {
        Util.setAllowDrawUnderStatusBar(window)
    }

    /**
     * This will set the color of the view with the id "status_bar" on KitKat and Lollipop. On
     * Lollipop if no such view is found it will set the statusbar color using the native method.
     *
     * @param color the new statusbar color (will be shifted down on Lollipop and above)
     */
    open fun setStatusBarColor(color: Int) {
        when {
            VersionUtils.hasMarshmallow() -> window.statusBarColor = color
            else -> window.statusBarColor = ColorUtil.darkenColor(color)
        }
        setLightStatusBarAuto(surfaceColor())
    }

    fun setStatusBarColorAuto() {
        // we don't want to use statusbar color because we are doing the color darkening on our own to support KitKat
        setStatusBarColor(surfaceColor())
        setLightStatusBarAuto(surfaceColor())
    }

    open fun setTaskDescriptionColor(@ColorInt color: Int) {
        ATH.setTaskDescriptionColor(this, color)
    }

    fun setTaskDescriptionColorAuto() {
        setTaskDescriptionColor(surfaceColor())
    }

    open fun setNavigationBarColor(color: Int) {
        ATH.setNavigationbarColor(this, color)
    }

    fun setNavigationBarColorAuto() {
        setNavigationBarColor(surfaceColor())
    }

    fun setNavigationBarDividerColorAuto() {
        ATH.setNavigationBarDividerColorAuto(this)
    }

    open fun setLightStatusBar(enabled: Boolean) {
        ATH.setLightStatusbar(this, enabled)
    }

    fun setLightStatusBarAuto(bgColor: Int) {
        setLightStatusBar(ColorUtil.isColorLight(bgColor))
    }

    fun setLightNavigationBar(enabled: Boolean) {
        ATH.setLightNavigationbar(this, enabled)
    }

    private fun unregisterSystemUiVisibility() {
        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSystemUiVisibility()
    }

    override fun attachBaseContext(newBase: Context?) {
        val code = PreferenceUtil.languageCode
        if (code != "auto") {
            super.attachBaseContext(LanguageContextWrapper.wrap(newBase, Locale(code)))
        } else super.attachBaseContext(newBase)
    }
}