package com.o4x.musical.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.R
import com.o4x.musical.extensions.themeOutlineColor
import kotlinx.android.synthetic.main.item_permission.view.*

class PermissionItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {



    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.PermissionItem, 0, 0)
        inflate(context, R.layout.item_permission, this)

        title.text = attributes.getText(R.styleable.PermissionItem_permissionTitle)
        summary.text = attributes.getText(R.styleable.PermissionItem_permissionTitleSubTitle)
        number.text = attributes.getText(R.styleable.PermissionItem_permissionTitleNumber)
        button.text = attributes.getText(R.styleable.PermissionItem_permissionButtonTitle)
        button.setIconResource(
            attributes.getResourceId(
                R.styleable.PermissionItem_permissionIcon,
                R.drawable.ic_album_white_24dp
            )
        )

        val color =
            attributes.getColor(R.styleable.PermissionItem_permissionColor, ThemeStore.themeColor(context))

        number.backgroundTintList = ColorStateList.valueOf(ColorUtil.withAlpha(color, 0.22f))

        button.themeOutlineColor(color)
        attributes.recycle()
    }

    fun setButtonClick(callBack: () -> Unit) {
        button.setOnClickListener { callBack.invoke() }
    }
}