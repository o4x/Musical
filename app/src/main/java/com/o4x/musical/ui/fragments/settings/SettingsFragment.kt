package com.o4x.musical.ui.fragments.settings

import android.content.Intent
import android.content.SharedPreferences
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import code.name.monkey.appthemehelper.ThemeStore
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.o4x.musical.R
import com.o4x.musical.extensions.backgroundColor
import com.o4x.musical.extensions.themeColor
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.preferences.LibraryPreferenceDialog
import com.o4x.musical.ui.dialogs.DeleteCachedDialog
import com.o4x.musical.ui.dialogs.DeleteCustomImagesDialog
import com.o4x.musical.ui.dialogs.SmartPlaylistLimitDialog
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.PreferenceUtil

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)

        listView.setBackgroundColor(backgroundColor())
        invalidateSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)
    }

    private fun invalidateSettings() {

          ////////////////////
         // THEME SETTINGS //
        ////////////////////
        val generalTheme: Preference? = findPreference(PreferenceUtil.GENERAL_THEME)
        var lastTheme = generalTheme?.summary.toString()
        generalTheme?.setOnPreferenceChangeListener { _, newValue ->
            val newTheme = newValue.toString()
            if (lastTheme != newTheme) {
                lastTheme = newTheme
                ThemeStore.markChanged(requireContext())
            }
            true
        }

        val themeColorPref: Preference = findPreference(PreferenceUtil.THEME_COLOR)!!
        val themeColor = themeColor()
//        themeColorPref.setColor(themeColor, ColorUtil.darkenColor(themeColor))

        themeColorPref.setOnPreferenceClickListener {
            ColorPickerDialogBuilder
                .with(context)
                .setTitle(R.string.theme_color)
                .initialColor(themeColor)
                .showAlphaSlider(false)
                .showBorder(true)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(14)
                .setPositiveButton(
                    R.string.ok
                ) { _, selectedColor, _ ->
                    if (ThemeStore.themeColor(requireContext()) != selectedColor)
                        ThemeStore.editTheme(requireContext()).themeColor(selectedColor).commit()
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .build()
                .show()
            return@setOnPreferenceClickListener true
        }

          ////////////////////////
         // INTERFACE SETTINGS //
        ////////////////////////

        val libraryPref: Preference = findPreference(PreferenceUtil.LIBRARY_CATEGORIES)!!
        libraryPref.setOnPreferenceClickListener {
            val fragment = LibraryPreferenceDialog.newInstance()
            fragment.show(childFragmentManager, libraryPref.key)
            return@setOnPreferenceClickListener true
        }

          ////////////////////
         // IMAGE SETTINGS //
        ////////////////////

        val deleteCachedPref: Preference = findPreference(PreferenceUtil.DELETE_CACHED_IMAGES)!!
        deleteCachedPref.setOnPreferenceClickListener {
            DeleteCachedDialog.create().show(
                childFragmentManager, it.key
            )
            return@setOnPreferenceClickListener true
        }

        val deleteCustomPref: Preference = findPreference(PreferenceUtil.DELETE_CUSTOM_IMAGES)!!
        deleteCustomPref.setOnPreferenceClickListener {
            DeleteCustomImagesDialog.create().show(
                childFragmentManager, it.key
            )
            return@setOnPreferenceClickListener true
        }

          ////////////////////
         // AUDIO SETTINGS //
        ////////////////////

        val findPreference: Preference = findPreference(PreferenceUtil.EQUALIZER)!!
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

          ///////////////////////////
         // NOTIFICATION SETTINGS //
        ///////////////////////////

        val classicNotification: TwoStatePreference? = findPreference(PreferenceUtil.CLASSIC_NOTIFICATION)
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

        val coloredNotification: TwoStatePreference? = findPreference(PreferenceUtil.COLORED_NOTIFICATION)
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

        val smartPlaylistPreference: Preference = findPreference(PreferenceUtil.SMART_PLAYLIST_LIMIT)!!
        setSummary(smartPlaylistPreference, PreferenceUtil.smartPlaylistLimit)
        smartPlaylistPreference.setOnPreferenceClickListener {
            SmartPlaylistLimitDialog.create().show(
                childFragmentManager, it.key
            )
            return@setOnPreferenceClickListener true
        }

        val languagePreference: Preference? = findPreference(PreferenceUtil.LANGUAGE_NAME)
        languagePreference?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            requireActivity().recreate()
            true
        }

        val aboutPreference: Preference? = findPreference(PreferenceUtil.ABOUT)
        aboutPreference?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_mainSettings_to_about)
            true
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.CLASSIC_NOTIFICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    findPreference<Preference>(PreferenceUtil.COLORED_NOTIFICATION)?.isEnabled =
                        sharedPreferences.getBoolean(key, false)
                }
            }
            PreferenceUtil.SMART_PLAYLIST_LIMIT -> {
                findPreference<Preference>(PreferenceUtil.SMART_PLAYLIST_LIMIT)?.summary =
                    PreferenceUtil.smartPlaylistLimit.toString()
            }
            PreferenceUtil.COLORED_FOOTER,
            PreferenceUtil.IGNORE_MEDIA -> MusicPlayerRemote.notifyMediaStoreChanged()
        }
    }


    private fun hasEqualizer(): Boolean {
        val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)

        val pm = requireActivity().packageManager
        val ri = pm.resolveActivity(effects, 0)
        return ri != null
    }


    private fun showProToastAndNavigate(message: String) {
        Toast.makeText(requireContext(), "$message is Pro version feature.", Toast.LENGTH_SHORT)
            .show()
        NavigationUtil.goToProVersion(requireActivity())
    }

    private fun setSummary(preference: Preference, value: Any?) {
        val stringValue = value.toString()
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            preference.setSummary(if (index >= 0) preference.entries[index] else null)
        } else {
            preference.summary = stringValue
        }
    }
}