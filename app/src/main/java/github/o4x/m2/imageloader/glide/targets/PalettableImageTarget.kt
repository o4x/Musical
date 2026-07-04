package github.o4x.m2.imageloader.glide.targets

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import github.o4x.m2.imageloader.glide.targets.palette.AbsPaletteTargetListener

class PalettableImageTarget(view: ImageView) : ImageViewTarget<Bitmap>(view) {

    private var listener: AbsPaletteTargetListener? = null

    // Only use in glide loader
    fun setListener(listener: AbsPaletteTargetListener?): PalettableImageTarget {
        this.listener = listener
        return this
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        super.onResourceReady(resource, transition)
        listener?.onResourceReady(resource)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        // Songs without artwork land here and show the char cover (the error
        // drawable); listeners that color the UI from it still need its palette.
        errorDrawable?.let {
            if (listener?.loadPlaceholderPalette == true)
                listener?.onResourceReady(it.toBitmap(1, 1))
        }
    }

    override fun setResource(resource: Bitmap?) {
        view.setImageBitmap(resource)
    }
}
