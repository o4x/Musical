package com.o4x.musical.util.theme

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import com.o4x.musical.R
import com.o4x.musical.extensions.generalThemeValue
import com.o4x.musical.util.theme.ThemeMode.*

object ThemeManager {

    @StyleRes
    fun getThemeResValue(
        context: Context
    ): Int = when (context.generalThemeValue) {
        LIGHT -> R.style.Theme_Musical_Light
        DARK -> R.style.Theme_Musical_Base
        BLACK -> R.style.Theme_Musical_Black
        AUTO -> R.style.Theme_Musical_FollowSystem
    }

    fun getNightMode(
        context: Context
    ): Int = when (context.generalThemeValue) {
        LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        DARK,
        BLACK -> AppCompatDelegate.MODE_NIGHT_YES
    }
}

