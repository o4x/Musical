package com.o4x.musical.imageloader.glide.targets;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.bumptech.glide.request.transition.Transition;
import com.o4x.musical.imageloader.glide.targets.palette.AbsPaletteTargetListener;

public class PlaceHolderBitmapTarget extends AbsImageBitmapTarget {

    private final AsyncTaskLoader<Bitmap> task;

    public PlaceHolderBitmapTarget(ImageView view, AbsPaletteTargetListener paletteTargetListener) {
        super(view, paletteTargetListener);

        task = new AsyncTaskLoader<Bitmap>(view.getContext()) {
            @Nullable
            @Override
            public Bitmap loadInBackground() {
                return listener.coverData.create(view.getContext());
            }

            @Override
            public void deliverResult(@Nullable Bitmap data) {
                super.deliverResult(data);
                setBackground(data);
            }
        };
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {
        super.onLoadStarted(placeholder);
        task.forceLoad();
    }

    @Override
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        super.onResourceReady(resource, transition);
        task.cancelLoad();
        setBackground(null);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        super.onLoadCleared(placeholder);
        task.cancelLoad();
    }

    private void setBackground(@Nullable Bitmap bitmap) {
        view.setBackground(new BitmapDrawable(bitmap));
        if (bitmap != null) {
            listener.onResourceReady(bitmap);
        }
    }
}
