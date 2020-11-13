package com.o4x.musical.helper

import android.content.Context
import androidx.palette.graphics.Palette
import code.name.monkey.appthemehelper.extensions.cardColor
import code.name.monkey.appthemehelper.extensions.surfaceColor
import code.name.monkey.appthemehelper.extensions.textColorPrimary
import code.name.monkey.appthemehelper.extensions.textColorSecondary
import com.o4x.musical.App
import com.o4x.musical.extensions.*

class MyPalette(context: Context, palette: Palette?) {

    private val domain = palette?.dominantSwatch
    val backgroundColor = domain?.rgb ?: context.cardColor()
    val textColorPrimary = domain?.bodyTextColor ?: context.textColorPrimary()
    val textColorSecondary = domain?.titleTextColor ?: context.textColorSecondary()

    val mightyColor = palette?.getVibrantColor(context.surfaceColor()) ?: context.surfaceColor()
}