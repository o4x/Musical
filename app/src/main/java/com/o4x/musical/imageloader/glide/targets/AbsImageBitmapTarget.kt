package com.o4x.musical.imageloader.glide.targets

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.request.target.ImageViewTarget
import com.o4x.musical.imageloader.glide.targets.palette.AbsPaletteTargetListener

class AbsImageBitmapTarget(view: ImageView?, private val listener: AbsPaletteTargetListener) :
    ImageViewTarget<Bitmap?>(view) {

    override fun setDrawable(drawable: Drawable?) {
        super.setDrawable(drawable)
        listener.onResourceReady(drawable?.toBitmap(1, 1))
    }

    override fun setResource(resource: Bitmap?) {
        view.setImageBitmap(resource)
        listener.onResourceReady(resource)
    }
}