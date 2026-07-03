package github.o4x.m2.extensions

import android.content.SharedPreferences

fun SharedPreferences.getStringOrDefault(key: String, default: String): String {
    return getString(key, default) ?: default
}
