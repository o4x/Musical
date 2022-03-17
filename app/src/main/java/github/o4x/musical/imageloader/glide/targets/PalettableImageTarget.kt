package github.o4x.musical.imageloader.glide.targets

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import github.o4x.musical.imageloader.glide.targets.palette.AbsPaletteTargetListener

class PalettableImageTarget(view: ImageView) : ImageViewTarget<Bitmap>(view) {

    private var listener: AbsPaletteTargetListener? = null
    // Only use in glide loader
    fun setListener(listener: AbsPaletteTargetListener?): PalettableImageTarget {
        this.listener = listener
        return this
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        super.onLoadStarted(placeholder)
        placeholder?.let {
            if (listener?.loadPlaceholderPalette == true)
                listener?.onResourceReady(it.toBitmap(1,1))
        }
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        super.onResourceReady(resource, transition)
        listener?.onResourceReady(resource)
    }

    override fun setResource(resource: Bitmap?) {
        view.setImageBitmap(resource)
    }
}