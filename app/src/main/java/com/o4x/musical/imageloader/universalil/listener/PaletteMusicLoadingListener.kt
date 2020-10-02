package com.o4x.musical.imageloader.universalil.listener

import android.graphics.Bitmap
import android.view.View
import com.nostra13.universalimageloader.core.assist.FailReason
import com.o4x.musical.util.color.MediaNotificationProcessor

abstract class PaletteMusicLoadingListener : AbsImageLoadingListener() {

    override fun onFailedBitmapReady(failedBitmap: Bitmap?) {
        super.onFailedBitmapReady(failedBitmap)
        onColorReady(MediaNotificationProcessor(context, failedBitmap))
    }

    override fun onLoadingCancelled(imageUri: String, view: View) {
        super.onLoadingCancelled(imageUri, view)
        onColorReady(MediaNotificationProcessor(context))
    }

    override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
        super.onLoadingComplete(imageUri, view, loadedImage)
        onColorReady(MediaNotificationProcessor(context, loadedImage))
    }

    abstract fun onColorReady(colors: MediaNotificationProcessor)
}