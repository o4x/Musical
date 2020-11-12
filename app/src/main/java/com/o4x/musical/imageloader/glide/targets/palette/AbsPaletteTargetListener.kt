package com.o4x.musical.imageloader.glide.targets.palette

import android.content.Context
import android.graphics.Bitmap

open class AbsPaletteTargetListener(val context: Context) {

    @JvmField
    var isSync = false

    open fun onResourceReady(resource: Bitmap?) {}
}