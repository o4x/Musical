package com.o4x.musical.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.o4x.musical.App
import com.o4x.musical.R

object EqualizerPref {

    private const val ENABLED = "enabled"


    private val ep: SharedPreferences =
        App.getContext().getSharedPreferences("key_equalizer_pref", Context.MODE_PRIVATE)

    @JvmStatic
    var isEqualizerEnabled: Boolean
        get() = ep.getBoolean(ENABLED, false)
        set(value) {
            val editor = ep.edit()
            editor.putBoolean(ENABLED, value)
            editor.apply()
        }
}