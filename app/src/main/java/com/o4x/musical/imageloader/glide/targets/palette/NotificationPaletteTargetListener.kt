
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