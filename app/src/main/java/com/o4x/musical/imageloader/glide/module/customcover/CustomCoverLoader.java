package com.o4x.musical.imageloader.glide.module.customcover;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.o4x.musical.imageloader.model.CoverData;

public class CustomCoverLoader implements ModelLoader<CoverData, Bitmap> {

    @Nullable
    @Override
    public LoadData<Bitmap> buildLoadData(@NonNull CoverData coverData, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(coverData), new CustomCoverFetcher(coverData));
    }

    @Override
    public boolean handles(@NonNull CoverData coverData) {
        return coverData.context != null;
    }

    public static class Factory implements ModelLoaderFactory<CoverData, Bitmap> {

        @NonNull
        @Override
        public ModelLoader<CoverData, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new CustomCoverLoader();
        }

        @Override
        public void teardown() {
        }
    }
}
