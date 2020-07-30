package com.o4x.musical.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.InputStream;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.o4x.musical.glide.artistimage.ArtistImage;
import com.o4x.musical.glide.artistimage.ArtistImageLoader;
import com.o4x.musical.glide.audiocover.AudioFileCover;
import com.o4x.musical.glide.audiocover.AudioFileCoverLoader;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
@GlideModule
public class PhonographGlideModule extends AppGlideModule {

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(AudioFileCover.class, InputStream.class, new AudioFileCoverLoader.Factory());
        registry.append(ArtistImage.class, InputStream.class, new ArtistImageLoader.Factory());
    }

}
