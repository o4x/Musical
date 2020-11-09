package com.o4x.musical.helper

import android.content.Context
import androidx.palette.graphics.Palette
import com.o4x.musical.App
import com.o4x.musical.extensions.*

class MyPalette(palette: Palette?) {
    private val context = App.getContext()

    private val domain = palette?.dominantSwatch
    val backgroundColor = domain?.rgb ?: context.cardColor()
    val textColorPrimary = domain?.bodyTextColor ?: context.textColorPrimary()
    val textColorSecondary = domain?.titleTextColor ?: context.textColorSecondary()

    val mightyColor = palette?.getVibrantColor(context.surfaceColor()) ?: context.surfaceColor()
}