package github.o4x.musical.helper

import android.content.Context
import androidx.palette.graphics.Palette
import github.o4x.musical.util.cardColor
import github.o4x.musical.util.surfaceColor
import github.o4x.musical.util.textColorPrimary
import github.o4x.musical.util.textColorSecondary

class MyPalette(context: Context, palette: Palette?) {

    private val domain = palette?.dominantSwatch
    val backgroundColor = domain?.rgb ?: context.cardColor()
    val textColorPrimary = domain?.bodyTextColor ?: context.textColorPrimary()
    val textColorSecondary = domain?.titleTextColor ?: context.textColorSecondary()

    val mightyColor = palette?.getVibrantColor(context.surfaceColor()) ?: context.surfaceColor()
}