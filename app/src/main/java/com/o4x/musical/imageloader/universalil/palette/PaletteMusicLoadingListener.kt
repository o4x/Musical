package com.o4x.musical.imageloader.universalil.palette

import android.graphics.Bitmap
import android.view.View
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.o4x.musical.util.color.MediaNotificationProcessor

abstract class PaletteMusicLoadingListener : AbsImageLoadingListener() {

    override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
        super.onLoadingFailed(imageUri, view, failReason)
        onColorReady(MediaNotificationProcessor(view.context, onFailedBitmap))
    }

    override fun onLoadingCancelled(imageUri: String, view: View) {
        super.onLoadingCancelled(imageUri, view)
        onColorReady(MediaNotificationProcessor(view.context))
    }

    override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
        super.onLoadingComplete(imageUri, view, loadedImage)
        onColorReady(MediaNotificationProcessor(view.context, loadedImage))
    }

    abstract fun onColorReady(colors: MediaNotificationProcessor)
}