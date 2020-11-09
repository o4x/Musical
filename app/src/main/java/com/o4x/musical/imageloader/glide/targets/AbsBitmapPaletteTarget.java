package com.o4x.musical.imageloader.glide.targets;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

public class AbsBitmapPaletteTarget extends ImageViewTarget<Bitmap> {

    protected final AbsPaletteTargetListener listener;

    public AbsBitmapPaletteTarget(ImageView view, AbsPaletteTargetListener paletteTargetListener) {
        super(view);
        this.listener = paletteTargetListener;
    }

    @Override
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        super.onResourceReady(resource, transition);
        listener.onResourceReady(resource);
    }

    @Override
    protected void setResource(@Nullable Bitmap resource) {
        view.setImageBitmap(resource);
    }
}
