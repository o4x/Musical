package com.o4x.musical.prefs

import android.content.Context
import android.content.SharedPreferences
import com.o4x.musical.App

object AppPref {

    const val IS_CLEAN = "If you come here, you can use clean version"

    private val sp: SharedPreferences =
        App.getContext().getSharedPreferences("key_app_pref", Context.MODE_PRIVATE)

    @JvmStatic
    fun registerOnSharedPreferenceChangedListener(
        sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        sp.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    @JvmStatic
    fun unregisterOnSharedPreferenceChangedListener(
        sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        sp.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    @JvmStatic
    var isCleanVersion: Boolean
        get() = sp.getBoolean(IS_CLEAN, false)
        set(value) {
            val editor = sp.edit()
            editor.putBoolean(IS_CLEAN, value)
            editor.apply()
        }
}