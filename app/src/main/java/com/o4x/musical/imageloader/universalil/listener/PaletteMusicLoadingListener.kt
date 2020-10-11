package com.o4x.musical.imageloader.universalil.listener

import android.graphics.Bitmap
import android.view.View
import com.nostra13.universalimageloader.core.assist.FailReason
import com.o4x.musical.util.color.MediaNotificationProcessor

abstract class PaletteMusicLoadingListener : AbsImageLoadingListener() {

    var mediaNotificationProcessor: MediaNotificationProcessor? = null

    override fun onLoadingStarted(imageUri: String?, view: View?) {
        super.onLoadingStarted(imageUri, view)
        mediaNotificationProcessor = MediaNotificationProcessor(context)
    }

    override fun onFailedBitmapReady(failedBitmap: Bitmap?) {
        super.onFailedBitmapReady(failedBitmap)
        colorReady(failedBitmap)
    }

    override fun onLoadingCancelled(imageUri: String, view: View) {
        super.onLoadingCancelled(imageUri, view)
        colorReady(null)
    }

    override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
        super.onLoadingComplete(imageUri, view, loadedImage)
        colorReady(loadedImage)
    }

    private fun colorReady(bitmap: Bitmap?) {
        mediaNotificationProcessor?.getPaletteAsync(
            { mediaNotificationProcessor ->
                if (mediaNotificationProcessor != null) {
                    onColorReady(mediaNotificationProcessor)
                }
            },
            bitmap
        )
    }

    abstract fun onColorReady(colors: MediaNotificationProcessor)
}