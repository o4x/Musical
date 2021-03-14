package com.o4x.musical.prefs

import android.content.Context
import android.content.SharedPreferences
import com.o4x.musical.App

object HomeHeaderPref {

    // types
    const val TYPE_CUSTOM = 0
    const val TYPE_SONG = 1
    const val TYPE_DEFAULT = 2

    const val CHANGE = "change"
    private const val TYPE = "type"
    private const val CUSTOM_IMAGE_PATH = "custom_image_path"
    private const val SONG_ID = "song_id"

    private val sp: SharedPreferences =
        App.getContext().getSharedPreferences("key_home_header_pref", Context.MODE_PRIVATE)

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
    var homeHeaderType: Int
        get() = sp.getInt(TYPE, TYPE_DEFAULT)
        private set(value) {
            val editor = sp.edit()
            editor.putInt(TYPE, value)
            editor.putLong(
                CHANGE,
                System.currentTimeMillis()
            )
            editor.apply()
        }

    @JvmStatic
    var customImagePath: String
        get() = sp.getString(CUSTOM_IMAGE_PATH, "/")!!
        set(value) {
            val editor = sp.edit()
            editor.putString(CUSTOM_IMAGE_PATH, value)
            editor.apply()
            homeHeaderType = TYPE_CUSTOM
        }

    @JvmStatic
    var imageSongID: Long
        get() = sp.getLong(SONG_ID, -1)
        set(value) {
            val editor = sp.edit()
            editor.putLong(SONG_ID, value)
            editor.apply()
            homeHeaderType = TYPE_SONG
        }

    @JvmStatic
    fun setDefault() {
        homeHeaderType = TYPE_DEFAULT
    }
}