package com.o4x.musical.imageloader.glide.module.customcover;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.o4x.musical.imageloader.model.CoverData;

public class CustomCoverFetcher implements DataFetcher<Bitmap> {

    final CoverData coverData;

    public CustomCoverFetcher(CoverData coverData) {
        this.coverData = coverData;
    }

    @Nullable
    private AsyncTaskLoader<Bitmap> task;

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
        if (coverData.context == null) return;

        task = new AsyncTaskLoader<Bitmap>(coverData.context) {
            @Nullable
            @Override
            public Bitmap loadInBackground() {
                return coverData.create(coverData.context);
            }

            @Override
            public void deliverResult(@Nullable Bitmap data) {
                super.deliverResult(data);
                callback.onDataReady(data);
            }
        };

        task.forceLoad();
    }

    @Override
    public void cleanup() {
        if (task != null) {
            task.cancelLoad();
        }
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancelLoad();
        }
    }

    @NonNull
    @Override
    public Class<Bitmap> getDataClass() {
        return Bitmap.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
