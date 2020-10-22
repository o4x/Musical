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
import code.name.monkey.appthemehelper.ThemeStore
import com.o4x.musical.R
import com.o4x.musical.extensions.themeBackgroundColor
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import kotlinx.android.synthetic.main.activity_permission.*

class PermissionActivity : AbsMusicServiceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView((R.layout.activity_permission))
        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()
        setLightNavigationBar(true)
        setTaskDescriptionColorAuto()
        setupTitle()

        storagePermission.setButtonClick {
            requestPermissions()
        }

        finish.themeBackgroundColor()
        finish.setOnClickListener {
            if (hasPermissions()) {
                startActivity(
                    Intent(this, MainActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                )
                finish()
            }
        }
    }

    private fun setupTitle() {
        val color = ThemeStore.themeColor(this)
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        val appName = HtmlCompat.fromHtml(
            "Hello there! <br>Welcome to <b><span  style='color:$hexColor';>Musical</span></b>",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        appNameText.text = appName
    }
}
