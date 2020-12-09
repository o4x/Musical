/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.o4x.musical.ui.activities.intro

import android.content.Intent
import android.os.Bundle
import androidx.core.text.HtmlCompat
import com.o4x.musical.R
import com.o4x.musical.extensions.themeBackgroundColor
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import kotlinx.android.synthetic.main.activity_permission.*
import kotlin.properties.Delegates

class PermissionActivity : AbsMusicServiceActivity() {

    private var baseColor by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView((R.layout.activity_permission))
        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()
        setLightNavigationBar(true)
        setTaskDescriptionColorAuto()

        baseColor = resources.getColor(R.color.deep_purple_A200)

        setupTitle()

        storagePermission.setButtonClick {
            requestPermissions()
        }

        finish.themeBackgroundColor(baseColor)
        finish.setOnClickListener {
            if (hasPermissions()) {
                finish()
            }
        }
    }

    private fun setupTitle() {
        val hexColor = String.format("#%06X", 0xFFFFFF and baseColor)
        val appName = HtmlCompat.fromHtml(
            "Welcome to <b><span  style='color:$hexColor';>Musical</span></b>" +
                    " <br/> " +
                    "We love to see you <span  style='color:$hexColor';>SMILE</span> :)" +
                    " <br/> " +
                    "I have not found another sentence yet.",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        appNameText.text = appName
    }
}
