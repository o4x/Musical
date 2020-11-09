package com.o4x.musical.imageloader.glide.targets;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.o4x.musical.imageloader.glide.targets.palette.AbsPaletteTargetListener;

public class AbsImageBitmapTarget extends ImageViewTarget<Bitmap> {

    protected final AbsPaletteTargetListener listener;

    public AbsImageBitmapTarget(ImageView view, AbsPaletteTargetListener paletteTargetListener) {
        super(view);
        this.listener = paletteTargetListener;
    }

    @Override
    protected void setResource(@Nullable Bitmap resource) {
        view.setImageBitmap(resource);
        listener.onResourceReady(resource);
    }
}
