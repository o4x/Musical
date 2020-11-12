
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
package com.o4x.musical.imageloader.glide.targets.palette

import android.content.Context
import android.graphics.Bitmap
import com.o4x.musical.App
import com.o4x.musical.util.color.MediaNotificationProcessor

abstract class NotificationPaletteTargetListener(context: Context) :
    AbsPaletteTargetListener(context) {

    var mediaNotificationProcessor: MediaNotificationProcessor? = null

    override fun onResourceReady(resource: Bitmap?) {
        if (resource == null) {
            val colors = MediaNotificationProcessor(context)
            onColorReady(colors)
        } else {
            if (isSync) {
                onColorReady(
                    MediaNotificationProcessor(context, resource)
                )
            } else {
                if (mediaNotificationProcessor == null) {
                    mediaNotificationProcessor = MediaNotificationProcessor(context)
                }
                mediaNotificationProcessor?.getPaletteAsync(
                    { onColorReady(it) }
                    , resource
                )
            }
        }
    }

    abstract fun onColorReady(colors: MediaNotificationProcessor)
}