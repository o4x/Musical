package com.o4x.musical.imageloader.glide.module;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.InputStream;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.o4x.musical.imageloader.glide.module.customcover.CustomCoverLoader;
import com.o4x.musical.imageloader.model.CoverData;
import com.o4x.musical.imageloader.model.MultiImage;
import com.o4x.musical.imageloader.glide.module.mosaicimage.MosaicImageLoader;
import com.o4x.musical.imageloader.model.AudioFileCover;
import com.o4x.musical.imageloader.glide.module.audiocover.AudioFileCoverLoader;

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
        builder.setLogLevel(Log.ERROR);
        super.applyOptions(context, builder);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(AudioFileCover.class, InputStream.class, new AudioFileCoverLoader.Factory());
        registry.append(CoverData.class, Bitmap.class, new CustomCoverLoader.Factory());
        registry.append(MultiImage.class, InputStream.class, new MosaicImageLoader.Factory());
        super.registerComponents(context, glide, registry);
    }

}