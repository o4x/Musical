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

package com.o4x.musical.extensions

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.annotation.AttrRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.MaterialValueHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.o4x.musical.R

fun Int.ripAlpha(): Int {
    return ColorUtil.stripAlpha(this)
}

fun Int.withAlpha(alpha: Float): Int {
    return ColorUtil.withAlpha(this, alpha)
}

fun Dialog.colorControlNormal() = resolveColor(android.R.attr.colorControlNormal)
fun Toolbar.backgroundTintList() {
    val surfaceColor = ATHUtil.resolveColor(context, R.attr.colorSurface, Color.BLACK)
    val colorStateList = ColorStateList.valueOf(surfaceColor)
    backgroundTintList = colorStateList
}

fun Context.themeColor() = ThemeStore.themeColor(this)
fun Fragment.themeColor() = ThemeStore.themeColor(requireContext())

fun Context.primaryColor() = resolveColor(R.attr.colorPrimary, Color.WHITE)
fun Fragment.primaryColor() = resolveColor(R.attr.colorPrimary, Color.WHITE)

fun Context.surfaceColor() = resolveColor(R.attr.colorSurface, Color.WHITE)
fun Fragment.surfaceColor() = resolveColor(R.attr.colorSurface, Color.WHITE)

fun Context.backgroundColor() = resolveColor(R.attr.backgroundColor, Color.WHITE)
fun Fragment.backgroundColor() = resolveColor(R.attr.backgroundColor, Color.WHITE)

fun Context.textColorSecondary() = resolveColor(android.R.attr.textColorSecondary)
fun Fragment.textColorSecondary() = resolveColor(android.R.attr.textColorSecondary)

fun Context.colorControlNormal() = resolveColor(android.R.attr.colorControlNormal)
fun Fragment.colorControlNormal() = resolveColor(android.R.attr.colorControlNormal)

fun Context.textColorPrimary() = resolveColor(android.R.attr.textColorPrimary)
fun Fragment.textColorPrimary() = resolveColor(android.R.attr.textColorPrimary)

fun Context.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    ATHUtil.resolveColor(this, attr, fallBackColor)
fun Fragment.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    ATHUtil.resolveColor(requireContext(), attr, fallBackColor)
fun Dialog.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    ATHUtil.resolveColor(context, attr, fallBackColor)


fun CheckBox.addthemeColor() {
    buttonTintList = ColorStateList.valueOf(ThemeStore.themeColor(context))
}
fun SeekBar.addthemeColor() {
    val colorState = ColorStateList.valueOf(ThemeStore.themeColor(context))
    progressTintList = colorState
    thumbTintList = colorState
}

fun Button.accentTextColor() {
    setTextColor(ThemeStore.themeColor(context))
}

fun ExtendedFloatingActionButton.themeColor() {
    val color = ThemeStore.themeColor(context)
    val textColor = MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(color))
    val colorStateList = ColorStateList.valueOf(color)
    val textColorStateList = ColorStateList.valueOf(textColor)
    backgroundTintList = colorStateList
    setTextColor(textColorStateList)
    iconTint = textColorStateList
}

fun MaterialButton.applyColor(color: Int) {
    val backgroundColorStateList = ColorStateList.valueOf(color)
    val textColorColorStateList = ColorStateList.valueOf(
        MaterialValueHelper.getPrimaryTextColor(
            context,
            ColorUtil.isColorLight(color)
        )
    )
    backgroundTintList = backgroundColorStateList
    setTextColor(textColorColorStateList)
    iconTint = textColorColorStateList
}

fun TextInputLayout.themeColor() {
    val themeColor = ThemeStore.themeColor(context)
    val colorState = ColorStateList.valueOf(themeColor)
    boxStrokeColor = themeColor
    defaultHintTextColor = colorState
    isHintAnimationEnabled = true
}

fun TextInputEditText.themeColor() {

}