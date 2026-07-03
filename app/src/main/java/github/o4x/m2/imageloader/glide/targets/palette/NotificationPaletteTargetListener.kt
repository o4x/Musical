
package github.o4x.m2.imageloader.glide.targets.palette

import android.content.Context
import android.graphics.Bitmap
import github.o4x.m2.App
import github.o4x.m2.util.color.MediaNotificationProcessor

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