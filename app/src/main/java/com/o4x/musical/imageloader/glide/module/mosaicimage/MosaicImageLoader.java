package com.o4x.musical.imageloader.glide.module.mosaicimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.o4x.musical.imageloader.model.MultiImage;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class MosaicImageLoader implements ModelLoader<MultiImage, InputStream> {

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull MultiImage multiImage, int width, int height, @NonNull Options options) {
        return  new LoadData<>(new ObjectKey(multiImage), new MosaicImageFetcher(multiImage));
    }

    @Override
    public boolean handles(@NonNull MultiImage multiImage) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<MultiImage, InputStream> {

        @NonNull
        @Override
        public ModelLoader<MultiImage, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new MosaicImageLoader();
        }

        @Override
        public void teardown() {

        }
    }
}

