/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.o4x.musical.ui.fragments.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import code.name.monkey.appthemehelper.ThemeStore
import com.o4x.musical.extensions.hide
import com.o4x.musical.extensions.show
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.App
import com.o4x.musical.R
import kotlinx.android.synthetic.main.fragment_main_settings.*

class MainSettingsFragment : Fragment(), View.OnClickListener {
    override fun onClick(view: View) {
        when (view.id) {
            R.id.generalSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_themeSettingsFragment)
            R.id.audioSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_audioSettings)
            R.id.personalizeSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_personalizeSettingsFragment)
            R.id.imageSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_imageSettingFragment)
            R.id.notificationSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_notificationSettingsFragment)
            R.id.otherSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_otherSettingsFragment)
            R.id.aboutSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_aboutActivity)
            R.id.nowPlayingSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_nowPlayingSettingsFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generalSettings.setOnClickListener(this)
        audioSettings.setOnClickListener(this)
        nowPlayingSettings.setOnClickListener(this)
        personalizeSettings.setOnClickListener(this)
        imageSettings.setOnClickListener(this)
        notificationSettings.setOnClickListener(this)
        otherSettings.setOnClickListener(this)
        aboutSettings.setOnClickListener(this)

        buyProContainer.apply {
            if (!App.isProVersion()) show() else hide()
            setOnClickListener {
                NavigationUtil.goToProVersion(requireContext())
            }
        }
        buyPremium.setOnClickListener {
            NavigationUtil.goToProVersion(requireContext())
        }
        ThemeStore.accentColor(requireContext()).let {
            buyPremium.setTextColor(it)
            diamondIcon.imageTintList = ColorStateList.valueOf(it)
        }
    }
}