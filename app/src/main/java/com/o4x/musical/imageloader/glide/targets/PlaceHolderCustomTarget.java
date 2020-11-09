package com.o4x.musical.imageloader.glide.targets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.o4x.musical.imageloader.glide.targets.palette.AbsPaletteTargetListener;

public class PlaceHolderCustomTarget extends CustomTarget<Bitmap> {

    private final Context context;
    private final int width;
    private final int height;
    private AsyncTaskLoader<Bitmap> task;

    private AbsPaletteTargetListener listener = new AbsPaletteTargetListener();
    public void setListener(AbsPaletteTargetListener listener) {
        this.listener = listener;

        listener.coverData.size = Math.max(width, height);
        task = new AsyncTaskLoader<Bitmap>(context) {
            @Nullable
            @Override
            public Bitmap loadInBackground() {
                return listener.coverData.create(context);
            }

            @Override
            public void deliverResult(@Nullable Bitmap data) {
                super.deliverResult(data);
                listener.onResourceReady(data);
                setResource(data);
            }
        };
    }

    public PlaceHolderCustomTarget(Context context, int width, int height) {
        super(width, height);
        this.context = context;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {
        super.onLoadStarted(placeholder);
        task.forceLoad();
    }

    @Override
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        task.cancelLoad();
        listener.onResourceReady(resource);
        setResource(resource);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        task.cancelLoad();
    }

    protected void setResource(Bitmap resource) {};
}
