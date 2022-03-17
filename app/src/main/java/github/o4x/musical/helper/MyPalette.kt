package github.o4x.musical.helper

import android.content.Context
import androidx.palette.graphics.Palette
import com.o4x.appthemehelper.extensions.cardColor
import com.o4x.appthemehelper.extensions.surfaceColor
import com.o4x.appthemehelper.extensions.textColorPrimary
import com.o4x.appthemehelper.extensions.textColorSecondary

class MyPalette(context: Context, palette: Palette?) {

    private val domain = palette?.dominantSwatch
    val backgroundColor = domain?.rgb ?: context.cardColor()
    val textColorPrimary = domain?.bodyTextColor ?: context.textColorPrimary()
    val textColorSecondary = domain?.titleTextColor ?: context.textColorSecondary()

    val mightyColor = palette?.getVibrantColor(context.surfaceColor()) ?: context.surfaceColor()
}