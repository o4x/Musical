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
package com.o4x.musical.imageloader.glide.targets

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import code.name.monkey.appthemehelper.util.ATHUtil
import com.bumptech.glide.request.transition.Transition
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.util.color.MediaNotificationProcessor

abstract class MusicColoredTarget(view: ImageView) : BitmapPaletteTarget(view) {

    protected val defaultFooterColor: Int
        get() = ATHUtil.resolveColor(getView().context, R.attr.colorControlNormal)

    abstract fun onColorReady(colors: MediaNotificationProcessor)

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        val colors = MediaNotificationProcessor(App.getContext(), errorDrawable)
        onColorReady(colors)
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        super.onResourceReady(resource, transition)
        MediaNotificationProcessor(
            App.getContext()).getPaletteAsync(
            { onColorReady(it) }
            , resource
        )
    }
}
