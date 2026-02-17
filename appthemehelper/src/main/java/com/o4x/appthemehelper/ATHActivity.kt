package com.o4x.appthemehelper

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class ATHActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeStore.prefs(this).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        ThemeStore.prefs(this).unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == ThemeStorePrefKeys.VALUES_CHANGED) {
            updateTheme()
        }
    }

    abstract fun updateTheme()
}