package github.o4x.m2.imageloader.glide.module

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import github.o4x.m2.imageloader.glide.module.artistimage.ArtistImage
import github.o4x.m2.imageloader.glide.module.artistimage.ArtistImageFactory
import github.o4x.m2.imageloader.glide.module.audiocover.AudioFileCoverLoader
import github.o4x.m2.imageloader.glide.module.mosaicimage.MosaicImageLoader
import github.o4x.m2.imageloader.model.AudioFileCover
import github.o4x.m2.imageloader.model.MultiImage
import java.io.InputStream

@GlideModule
class MyGlideModule : AppGlideModule() {

    override fun isManifestParsingEnabled(): Boolean = false

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.ERROR)
        super.applyOptions(context, builder)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(ArtistImage::class.java, InputStream::class.java, ArtistImageFactory(context))
        registry.append(AudioFileCover::class.java, InputStream::class.java, AudioFileCoverLoader.Factory())
        registry.append(MultiImage::class.java, InputStream::class.java, MosaicImageLoader.Factory())
        super.registerComponents(context, glide, registry)
    }
}
