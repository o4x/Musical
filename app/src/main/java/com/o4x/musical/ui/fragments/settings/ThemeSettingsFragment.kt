/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.o4x.musical.ui.fragments.settings

import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEColorPreference
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.VersionUtils
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.appshortcuts.DynamicShortcutManager
import com.o4x.musical.util.PreferenceUtil
import com.o4x.musical.util.PreferenceUtil.DESATURATED_COLOR

/**
 * @author Hemanth S (h4h13).
 */

class ThemeSettingsFragment : AbsSettingsFragment() {
    override fun invalidateSettings() {
        val generalTheme: Preference? = findPreference("general_theme")
        generalTheme?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                val theme = newValue as String
                setSummary(it, newValue)
                ThemeStore.markChanged(requireContext())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    requireActivity().setTheme(PreferenceUtil.themeResFromPrefValue(theme))
                    DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                }
                requireActivity().recreate()
                true
            }
        }

        val accentColorPref: ATEColorPreference = findPreference("accent_color")!!
        val accentColor = ThemeStore.accentColor(requireContext())
        accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor))

        accentColorPref.setOnPreferenceClickListener {
            ColorChooserDialog.Builder(requireContext(), R.string.accent_color)
                .accentMode(true)
                .allowUserColorInput(true)
                .allowUserColorInputAlpha(false)
                .preselect(accentColor)
                .show(requireActivity())
            return@setOnPreferenceClickListener true
        }
        val blackTheme: ATESwitchPreference? = findPreference("black_theme")
        blackTheme?.setOnPreferenceChangeListener { _, _ ->
            if (!App.isProVersion()) {
                showProToastAndNavigate("Just Black theme")
                return@setOnPreferenceChangeListener false
            }
            ThemeStore.markChanged(requireContext())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                requireActivity().setTheme(PreferenceUtil.themeResFromPrefValue("black"))
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
            }
            requireActivity().recreate()
            true
        }

        val desaturatedColor: ATESwitchPreference? = findPreference(DESATURATED_COLOR)
        desaturatedColor?.setOnPreferenceChangeListener { _, value ->
            val desaturated = value as Boolean
            ThemeStore.prefs(requireContext())
                .edit()
                .putBoolean("desaturated_color", desaturated)
                .apply()
            PreferenceUtil.isDesaturatedColor = desaturated
            requireActivity().recreate()
            true
        }


        val colorAppShortcuts: TwoStatePreference = findPreference("should_color_app_shortcuts")!!
        if (!VersionUtils.hasNougatMR()) {
            colorAppShortcuts.isVisible = false
        } else {
            colorAppShortcuts.isChecked = PreferenceUtil.isColoredAppShortcuts
            colorAppShortcuts.setOnPreferenceChangeListener { _, newValue ->
                PreferenceUtil.isColoredAppShortcuts = newValue as Boolean
                DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                true
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }
}
