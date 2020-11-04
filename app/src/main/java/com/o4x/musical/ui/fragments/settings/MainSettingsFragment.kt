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

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import code.name.monkey.appthemehelper.ColorPalette
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEColorPreference
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEListPreference
import code.name.monkey.appthemehelper.util.ColorUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorSelectedListener
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.o4x.musical.R
import com.o4x.musical.extensions.themeColor
import com.o4x.musical.ui.dialogs.ChangeSmartPlaylistLimit
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.PreferenceUtil
import com.o4x.musical.util.PreferenceUtil.smartPlaylistLimit

class MainSettingsFragment : AbsSettingsFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // THEME PREFS //
        addPreferencesFromResource(R.xml.pref_theme)
        // AUDIO PREFS //
        addPreferencesFromResource(R.xml.pref_audio)
        // UI PREFS //
        addPreferencesFromResource(R.xml.pref_ui)
        // IMAGE PREFS //
        addPreferencesFromResource(R.xml.pref_images)
        // NOTIFICATION PREFS //
        addPreferencesFromResource(R.xml.pref_notification)
        // ADVANCED PREFS //
        addPreferencesFromResource(R.xml.pref_advanced)
    }

    override fun invalidateSettings() {
          ////////////////////
         // THEME SETTINGS //
        ////////////////////
        val generalTheme: Preference? = findPreference("general_theme")
        var lastTheme = generalTheme?.summary.toString()
        generalTheme?.setOnPreferenceChangeListener { _, newValue ->
            val newTheme = newValue.toString()
            if (lastTheme != newTheme) {
                lastTheme = newTheme
                ThemeStore.markChanged(requireContext())
            }
            true
        }

        val themeColorPref: ATEColorPreference = findPreference("theme_color")!!
        val themeColor = themeColor()
        themeColorPref.setColor(themeColor, ColorUtil.darkenColor(themeColor))

        themeColorPref.setOnPreferenceClickListener {
            ColorPickerDialogBuilder
                .with(context)
                .setTitle(R.string.theme_color)
                .initialColor(themeColor)
                .showAlphaSlider(false)
                .showBorder(true)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(14)
                .setPositiveButton(R.string.ok
                ) { _, selectedColor, _ ->
                    if (ThemeStore.themeColor(requireContext()) != selectedColor)
                        ThemeStore.editTheme(requireContext()).themeColor(selectedColor).commit()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .build()
                .show()
            return@setOnPreferenceClickListener true
        }

          ////////////////////
         // AUDIO SETTINGS //
        ////////////////////

        val findPreference: Preference = findPreference("equalizer")!!
        if (!hasEqualizer()) {
            findPreference.isEnabled = false
            findPreference.summary = resources.getString(R.string.no_equalizer)
        } else {
            findPreference.isEnabled = true
        }
        findPreference.setOnPreferenceClickListener {
            NavigationUtil.openEqualizer(requireActivity())
            true
        }

        val homeArtistStyle: ATEListPreference? = findPreference("home_artist_grid_style")
        homeArtistStyle?.setOnPreferenceChangeListener { preference, newValue ->
            setSummary(preference, newValue)
            true
        }

          ////////////////////
         // IMAGE SETTINGS //
        ////////////////////

        val autoDownloadImagesPolicy: Preference = findPreference("auto_download_images_policy")!!
        setSummary(autoDownloadImagesPolicy)
        autoDownloadImagesPolicy.setOnPreferenceChangeListener { _, o ->
            setSummary(autoDownloadImagesPolicy, o)
            true
        }

          ///////////////////////////
         // NOTIFICATION SETTINGS //
        ///////////////////////////

        val classicNotification: TwoStatePreference? = findPreference("classic_notification")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            classicNotification?.isVisible = false
        } else {
            classicNotification?.apply {
                isChecked = PreferenceUtil.isClassicNotification
                setOnPreferenceChangeListener { _, newValue ->
                    // Save preference
                    PreferenceUtil.isClassicNotification = newValue as Boolean
                    invalidateSettings()
                    true
                }
            }
        }

        val coloredNotification: TwoStatePreference? = findPreference("colored_notification")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            coloredNotification?.isEnabled = PreferenceUtil.isClassicNotification
        } else {
            coloredNotification?.apply {
                isChecked = PreferenceUtil.isColoredNotification
                setOnPreferenceChangeListener { _, newValue ->
                    PreferenceUtil.isColoredNotification = newValue as Boolean
                    true
                }
            }
        }

          ///////////////////////
         // ADVANCED SETTINGS //
        ///////////////////////

        val smartPlaylistPreference: Preference = findPreference("smart_playlist_limit")!!
        setSummary(smartPlaylistPreference, smartPlaylistLimit)
        smartPlaylistPreference.setOnPreferenceClickListener {
            ChangeSmartPlaylistLimit.create().show(
                childFragmentManager, "SETTINGS"
            )
            return@setOnPreferenceClickListener true
        }

        val languagePreference: Preference? = findPreference("language_name")
        languagePreference?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            requireActivity().recreate()
            true
        }
        val aboutPreference: Preference? = findPreference("about")
        aboutPreference?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_mainSettings_to_about)
            true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.CLASSIC_NOTIFICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    findPreference<Preference>("colored_notification")?.isEnabled =
                        sharedPreferences.getBoolean(key, false)
                }
            }
            PreferenceUtil.SMART_PLAYLIST_LIMIT -> {
                findPreference<Preference>("smart_playlist_limit")?.summary =
                    smartPlaylistLimit.toString()
            }
        }
    }


    private fun hasEqualizer(): Boolean {
        val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)

        val pm = requireActivity().packageManager
        val ri = pm.resolveActivity(effects, 0)
        return ri != null
    }
}