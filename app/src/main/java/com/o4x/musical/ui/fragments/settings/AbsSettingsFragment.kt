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

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat
import code.name.monkey.appthemehelper.util.ATHUtil
import com.o4x.musical.R
import com.o4x.musical.preferences.*
import com.o4x.musical.util.NavigationUtil

/**
 * @author Hemanth S (h4h13).
 */

abstract class AbsSettingsFragment : ATEPreferenceFragmentCompat() {

    internal fun showProToastAndNavigate(message: String) {
        Toast.makeText(requireContext(), "$message is Pro version feature.", Toast.LENGTH_SHORT)
            .show()
        NavigationUtil.goToProVersion(requireActivity())
    }

    internal fun setSummary(preference: Preference, value: Any?) {
        val stringValue = value.toString()
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            preference.setSummary(if (index >= 0) preference.entries[index] else null)
        } else {
            preference.summary = stringValue
        }
    }

    protected fun setSummary(preference: Preference?) {
        preference?.let {
            setSummary(
                it, PreferenceManager
                    .getDefaultSharedPreferences(it.context)
                    .getString(it.key, "")
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.setBackgroundColor(ATHUtil.resolveColor(requireContext(), R.attr.colorSurface))
        invalidateSettings()
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        when (preference) {
            is LibraryPreference -> {
                val fragment = LibraryPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }
            is NowPlayingScreenPreference -> {
                val fragment = NowPlayingScreenPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }
            is AlbumCoverStylePreference -> {
                val fragment = AlbumCoverStylePreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }
            is BlacklistPreference -> {
                val fragment = BlacklistPreferenceDialog.newInstance()
                fragment.show(childFragmentManager, preference.key)
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }


    abstract fun invalidateSettings()
}
