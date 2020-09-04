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

import android.content.Intent
import android.content.SharedPreferences
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import code.name.monkey.appthemehelper.ColorPalette
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEColorPreference
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEListPreference
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import code.name.monkey.appthemehelper.util.ColorUtil
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.appshortcuts.DynamicShortcutManager
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.PreferenceUtil

class MainSettingsFragment : AbsSettingsFragment(), View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

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
        // NOW PLAYING SCREEN PREFS //
        addPreferencesFromResource(R.xml.pref_now_playing_screen)
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

        val themeColorPref: ATEColorPreference = findPreference("theme_color")!!
        val themeColor = ThemeStore.themeColor(requireContext())
        themeColorPref.setColor(themeColor, ColorUtil.darkenColor(themeColor))

        themeColorPref.setOnPreferenceClickListener {
            ColorChooserDialog.Builder(requireContext(), R.string.theme_color)
                .customColors(ColorPalette(requireActivity()).materialColorsPrimary, ColorPalette(requireActivity()).materialColors)
                .accentMode(true)
                .allowUserColorInput(true)
                .allowUserColorInputAlpha(false)
                .preselect(themeColor)
                .show(requireActivity())
            return@setOnPreferenceClickListener true
        }

          /////////////////////////////////
         // NOW PLAYING SCREEN SETTINGS //
        /////////////////////////////////

        val nowScreenPreference: Preference? = findPreference(PreferenceUtil.NOW_PLAYING_SCREEN_ID)
        nowScreenPreference?.setSummary(PreferenceUtil.nowPlayingScreen.titleRes)

        val albumCoverPreference: Preference? = findPreference(PreferenceUtil.ALBUM_COVER_STYLE)
        albumCoverPreference?.setSummary(PreferenceUtil.albumCoverStyle.titleRes)

        val carouselEffect: TwoStatePreference = findPreference("carousel_effect")!!
        carouselEffect.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean && !App.isProVersion()) {
                showProToastAndNavigate(getString(R.string.pref_title_toggle_carousel_effect))
                return@setOnPreferenceChangeListener false
            }
            return@setOnPreferenceChangeListener true
        }

        val albumTransformPreference: Preference? = findPreference("album_cover_transform")
        albumTransformPreference?.setOnPreferenceChangeListener { albumPrefs, newValue ->
            setSummary(albumPrefs, newValue)
            true
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
        val tabTextMode: ATEListPreference? = findPreference("tab_text_mode")
        tabTextMode?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
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

        val preference: Preference? = findPreference("last_added_interval")
        preference?.setOnPreferenceChangeListener { lastAdded, newValue ->
            setSummary(lastAdded, newValue)
            true
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

    override fun onClick(view: View) {
//        when (view.id) {
//            R.id.generalSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_themeSettingsFragment)
//            R.id.audioSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_audioSettings)
//            R.id.personalizeSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_personalizeSettingsFragment)
//            R.id.imageSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_imageSettingFragment)
//            R.id.notificationSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_notificationSettingsFragment)
//            R.id.otherSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_otherSettingsFragment)
//            R.id.aboutSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_aboutActivity)
//            R.id.nowPlayingSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_nowPlayingSettingsFragment)
//        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.NOW_PLAYING_SCREEN_ID -> invalidateSettings()
            PreferenceUtil.ALBUM_COVER_STYLE -> invalidateSettings()
            PreferenceUtil.CIRCULAR_ALBUM_ART, PreferenceUtil.CAROUSEL_EFFECT -> invalidateSettings()
            PreferenceUtil.CLASSIC_NOTIFICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    findPreference<Preference>("colored_notification")?.isEnabled =
                        sharedPreferences?.getBoolean(key, false)!!
                }
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