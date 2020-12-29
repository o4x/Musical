package com.o4x.musical.imageloader.glide.module;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.o4x.musical.imageloader.glide.module.artistimage.ArtistImage;
import com.o4x.musical.imageloader.glide.module.artistimage.ArtistImageFactory;
import com.o4x.musical.imageloader.glide.module.audiocover.AudioFileCoverLoader;
import com.o4x.musical.imageloader.glide.module.mosaicimage.MosaicImageLoader;
import com.o4x.musical.imageloader.model.AudioFileCover;
import com.o4x.musical.imageloader.model.MultiImage;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
@GlideModule
public class MyGlideModule extends AppGlideModule {

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
//        builder.setLogLevel(Log.ERROR);
        super.applyOptions(context, builder);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(ArtistImage.class, InputStream.class, new ArtistImageFactory(context));
        registry.append(AudioFileCover.class, InputStream.class, new AudioFileCoverLoader.Factory());
        registry.append(MultiImage.class, InputStream.class, new MosaicImageLoader.Factory());
        super.registerComponents(context, glide, registry);
    }

}
