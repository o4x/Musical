package github.o4x.musical.imageloader.glide.targets.palette

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import androidx.palette.graphics.Palette
import github.o4x.musical.helper.MyPalette

abstract class PaletteTargetListener(context: Context) : AbsPaletteTargetListener(context) {

    private var paletteAsyncTask: AsyncTask<Bitmap, Void, Palette>? = null

    override fun onResourceReady(resource: Bitmap?) {
        if (paletteAsyncTask != null) paletteAsyncTask!!.cancel(false)
        if (resource == null) {
            onColorReady(MyPalette(context, null), null)
        } else {
            val paletteBuilder = Palette.from(resource)
            if (isSync) {
                onColorReady(
                    MyPalette(
                        context,
                        paletteBuilder.generate()
                    ), resource
                )
            } else {
                paletteAsyncTask = paletteBuilder.generate { palette: Palette? ->
                    onColorReady(
                        MyPalette(
                            context,
                            palette
                        ), resource
                    )
                }
            }
        }
    }

    abstract fun onColorReady(colors: MyPalette, resource: Bitmap?)
}