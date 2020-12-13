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

import android.os.Bundle
import androidx.core.text.HtmlCompat
import code.name.monkey.appthemehelper.extensions.accentColor
import com.o4x.musical.databinding.ActivityPermissionBinding
import com.o4x.musical.shared.Permissions
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity

class PermissionActivity : AbsMusicServiceActivity() {

    val binding by lazy { ActivityPermissionBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()
        setLightNavigationBar(true)
        setTaskDescriptionColorAuto()

        setupTitle()
        update()

        binding.storagePermission.setButtonClick {
            requestPermissions()
        }

        binding.finish.setOnClickListener {
            if (hasPermissions()) {
                finish()
            }
        }
    }

    private fun setupTitle() {
        val hexColor = String.format("#%06X", 0xFFFFFF and accentColor())
        val appName = HtmlCompat.fromHtml(
            "Welcome to <b><span  style='color:$hexColor';>Musical</span></b>" +
                    " <br/> " +
                    "We love to see you <span  style='color:$hexColor';>SMILE</span> :)",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        binding.appNameText.text = appName
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        update()
    }

    private fun update() {
        binding.finish.isEnabled = Permissions.canReadStorage(this)
    }
}
