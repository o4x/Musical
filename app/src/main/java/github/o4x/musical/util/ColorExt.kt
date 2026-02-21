package github.o4x.musical.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.fragment.app.Fragment
import com.google.android.material.R
import com.google.android.material.color.MaterialColors

fun Int.ripAlpha(): Int {
    return ColorUtil.stripAlpha(this)
}

fun Int.withAlpha(alpha: Float): Int {
    return ColorUtil.withAlpha(this, alpha)
}

fun Context.primaryColor() = resolveColor(R.attr.colorPrimaryFixed, Color.WHITE)
fun Fragment.primaryColor() = resolveColor(R.attr.colorPrimaryFixed, Color.WHITE)

fun Context.accentColor() = resolveColor(R.attr.colorOnTertiary, Color.WHITE)
fun Fragment.accentColor() = resolveColor(R.attr.colorOnTertiary, Color.WHITE)

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

fun Context.cardColor() = resolveColor(R.attr.backgroundColor)
fun Fragment.cardColor() = resolveColor(R.attr.backgroundColor)

fun Context.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    MaterialColors.getColor(
        this,
        attr,
        fallBackColor
    )
fun Fragment.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    MaterialColors.getColor(
        requireContext(),
        attr,
        fallBackColor
    )
fun Dialog.resolveColor(@AttrRes attr: Int, fallBackColor: Int = 0) =
    MaterialColors.getColor(
        context,
        attr,
        fallBackColor
    )