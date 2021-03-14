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

package com.o4x.appthemehelper.extensions

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
import com.o4x.appthemehelper.R
import com.o4x.appthemehelper.ThemeStore
import com.o4x.appthemehelper.util.ATHUtil
import com.o4x.appthemehelper.util.ColorUtil
import com.o4x.appthemehelper.util.MaterialValueHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

fun Int.ripAlpha(): Int {
    return ColorUtil.stripAlpha(this)
}

fun Int.withAlpha(alpha: Float): Int {
    return ColorUtil.withAlpha(this, alpha)
}

fun Context.primaryColor() = resolveColor(R.attr.colorPrimary, Color.WHITE)
fun Fragment.primaryColor() = resolveColor(R.attr.colorPrimary, Color.WHITE)

fun Context.accentColor() = resolveColor(R.attr.colorAccent, Color.WHITE)
fun Fragment.accentColor() = resolveColor(R.attr.colorAccent, Color.WHITE)

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

fun Context.textColorTertiary() = resolveColor(android.R.attr.textColorTertiary)
fun Fragment.textColorTertiary() = resolveColor(android.R.attr.textColorTertiary)

fun Context.cardColor() = resolveColor(R.attr.cardBackgroundColor)
fun Fragment.cardColor() = resolveColor(R.attr.cardBackgroundColor)

fun Context.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    ATHUtil.resolveColor(this, attr, fallBackColor)
fun Fragment.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    ATHUtil.resolveColor(requireContext(), attr, fallBackColor)
fun Dialog.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    ATHUtil.resolveColor(context, attr, fallBackColor)