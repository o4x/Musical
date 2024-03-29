package com.o4x.appthemehelper.util

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import com.o4x.appthemehelper.ThemeStore
import com.o4x.appthemehelper.extensions.accentColor
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
object NavigationViewUtil {

    fun setItemIconColors(navigationView: NavigationView, @ColorInt normalColor: Int, @ColorInt selectedColor: Int) {
        val iconSl = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(normalColor, selectedColor)
        )
        navigationView.itemIconTintList = iconSl
        val drawable = navigationView.itemBackground
        navigationView.itemBackground = TintHelper.createTintedDrawable(
            drawable,
            ColorUtil.withAlpha(navigationView.context.accentColor(), 0.2f)
        )
    }

    fun setItemTextColors(navigationView: NavigationView, @ColorInt normalColor: Int, @ColorInt selectedColor: Int) {
        val textSl = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
            intArrayOf(normalColor, selectedColor)
        )
        navigationView.itemTextColor = textSl
    }

    fun setItemIconColors(bottomNavigationView: BottomNavigationView, @ColorInt normalColor: Int, @ColorInt selectedColor: Int) {
        val iconSl = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
            intArrayOf(normalColor, selectedColor)
        )
        bottomNavigationView.itemIconTintList = iconSl
    }

    fun setItemTextColors(bottomNavigationView: BottomNavigationView, @ColorInt normalColor: Int, @ColorInt selectedColor: Int) {
        val textSl = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
            intArrayOf(normalColor, selectedColor)
        )
        bottomNavigationView.itemTextColor = textSl
    }
}