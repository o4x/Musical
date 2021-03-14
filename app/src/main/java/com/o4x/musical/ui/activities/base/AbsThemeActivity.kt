package com.o4x.musical.ui.activities.base

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import com.o4x.appthemehelper.ATH
import com.o4x.appthemehelper.ATHActivity
import com.o4x.appthemehelper.extensions.surfaceColor
import com.o4x.appthemehelper.util.ColorUtil
import com.o4x.musical.LanguageContextWrapper
import com.o4x.musical.appshortcuts.DynamicShortcutManager
import com.o4x.musical.prefs.PreferenceUtil
import com.o4x.musical.prefs.PreferenceUtil.nightMode
import com.o4x.musical.util.Util
import java.util.*

abstract class AbsThemeActivity : ATHActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        theme.applyStyle(PreferenceUtil.getThemeColorRes(), true)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
    }

    open fun setTheme() {
        setTheme(PreferenceUtil.getGeneralThemeRes())
//        setDefaultNightMode(nightMode) With tapsel main activity call twice
        DynamicShortcutManager(this).updateDynamicShortcuts()
    }

    override fun updateTheme() {
        recreate()
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
        window.statusBarColor = ColorUtil.darkenColor(color)
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
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        if (key == PreferenceUtil.LANGUAGE_NAME) {
            recreate()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val code = PreferenceUtil.languageCode
        if (code != "auto") {
            super.attachBaseContext(LanguageContextWrapper.wrap(newBase, Locale(code)))
        } else {
            super.attachBaseContext(
                LanguageContextWrapper.wrap(newBase,
                    Locale(Resources.getSystem().configuration.locale.language)))
        }
    }
}