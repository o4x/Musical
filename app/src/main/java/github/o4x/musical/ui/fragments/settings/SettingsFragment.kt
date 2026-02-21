package github.o4x.musical.ui.fragments.settings

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import github.o4x.musical.R
import github.o4x.musical.helper.MusicPlayerRemote
import github.o4x.musical.preferences.LibraryPreferenceDialog
import github.o4x.musical.ui.dialogs.DeleteCachedDialog
import github.o4x.musical.ui.dialogs.DeleteCustomImagesDialog
import github.o4x.musical.ui.dialogs.SmartPlaylistLimitDialog
import github.o4x.musical.prefs.PreferenceUtil

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
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
//        val darkMode: Preference? = findPreference(PreferenceUtil.DARK_MODE)
//        var lastTheme = darkMode?.summary.toString()
//        darkMode?.setOnPreferenceChangeListener { _, newValue ->
//            val newTheme = newValue.toString()
//            if (lastTheme != newTheme) {
//                lastTheme = newTheme
//                ThemeStore.markChanged(requireContext())
//            }
//            true
//        }

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

          ///////////////////////////
         // NOTIFICATION SETTINGS //
        ///////////////////////////

        val coloredNotification: TwoStatePreference? = findPreference(PreferenceUtil.COLORED_NOTIFICATION)

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
            true
        }

        val aboutPreference: Preference? = findPreference(PreferenceUtil.ABOUT)
        aboutPreference?.setOnPreferenceClickListener {
            try {
                findNavController().navigate(R.id.action_mainSettings_to_about)
            } catch (e: Exception) {}
            true
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceUtil.SMART_PLAYLIST_LIMIT -> {
                findPreference<Preference>(PreferenceUtil.SMART_PLAYLIST_LIMIT)?.summary =
                    PreferenceUtil.smartPlaylistLimit.toString()
            }
            PreferenceUtil.COLORED_FOOTER,
            PreferenceUtil.IGNORE_MEDIA -> MusicPlayerRemote.notifyMediaStoreChanged()
        }
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