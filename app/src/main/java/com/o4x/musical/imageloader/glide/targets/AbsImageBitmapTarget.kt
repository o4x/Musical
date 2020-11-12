package com.o4x.musical.imageloader.glide.targets

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.o4x.musical.imageloader.glide.targets.palette.AbsPaletteTargetListener

class AbsImageBitmapTarget(view: ImageView?, private val listener: AbsPaletteTargetListener) :
    ImageViewTarget<Bitmap?>(view) {

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
        super.onResourceReady(resource, transition)
        listener.onResourceReady(resource)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        listener.onResourceReady(null)
    }

    override fun setResource(resource: Bitmap?) {
        view.setImageBitmap(resource)
    }
}