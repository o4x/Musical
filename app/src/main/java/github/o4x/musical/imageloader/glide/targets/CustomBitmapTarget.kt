package github.o4x.musical.imageloader.glide.targets

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.CallSuper
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import github.o4x.musical.imageloader.glide.targets.palette.AbsPaletteTargetListener

open class CustomBitmapTarget(
    private val width: Int,
    private val height: Int
) : CustomTarget<Bitmap>(
    width, height
) {

    private var listener: AbsPaletteTargetListener? = null
    // Only use in glide loader
    fun setListener(listener: AbsPaletteTargetListener?): CustomBitmapTarget {
        this.listener = listener
        return this
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        setResource(resource)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        setResource(errorDrawable)
    }

    override fun onLoadCleared(placeholder: Drawable?) {}

    private fun setResource(drawable: Drawable?) {
        drawable?.let {
            setResource(
                it.toBitmap(width, height)
            )
        }
    }

    @CallSuper
    open fun setResource(resource: Bitmap) {
        listener?.onResourceReady(resource)
    }
}