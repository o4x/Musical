package github.o4x.musical.extensions

import android.app.Dialog
import android.content.res.ColorStateList
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.appcompat.widget.Toolbar
import com.o4x.appthemehelper.extensions.accentColor
import com.o4x.appthemehelper.extensions.resolveColor
import com.o4x.appthemehelper.extensions.surfaceColor
import com.o4x.appthemehelper.util.ColorUtil
import com.o4x.appthemehelper.util.MaterialValueHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

fun Dialog.colorControlNormal() = resolveColor(android.R.attr.colorControlNormal)

fun Toolbar.backgroundTintList() {
    val surfaceColor = context.surfaceColor()
    val colorStateList = ColorStateList.valueOf(surfaceColor)
    backgroundTintList = colorStateList
}

fun CheckBox.addthemeColor() {
    buttonTintList = ColorStateList.valueOf(context.accentColor())
}
fun SeekBar.addthemeColor() {
    val colorState = ColorStateList.valueOf(context.accentColor())
    progressTintList = colorState
    thumbTintList = colorState
}

fun MaterialButton.themeBackgroundColor() {
    backgroundTintList = ColorStateList.valueOf(context.accentColor())
}

fun MaterialButton.themeBackgroundColor(color: Int) {
    backgroundTintList = ColorStateList.valueOf(color)
}

fun MaterialButton.themeOutlineColor(color: Int) {
    val colorStateList = ColorStateList.valueOf(color)
    iconTint = colorStateList
    strokeColor = colorStateList
    setTextColor(colorStateList)
    rippleColor = colorStateList
}

fun MaterialButton.themeOutlineColor() {
    val color = context.accentColor()
    val colorStateList = ColorStateList.valueOf(color)
    iconTint = colorStateList
    strokeColor = colorStateList
    setTextColor(colorStateList)
    rippleColor = colorStateList
}

fun Button.accentTextColor() {
    setTextColor(context.accentColor())
}

fun ExtendedFloatingActionButton.accentColor() {
    val color = context.accentColor()
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

fun TextInputLayout.accentColor() {
    val accentColor = context.accentColor()
    val colorState = ColorStateList.valueOf(accentColor)
    boxStrokeColor = accentColor
    defaultHintTextColor = colorState
    isHintAnimationEnabled = true
}

fun TextInputEditText.accentColor() {

}