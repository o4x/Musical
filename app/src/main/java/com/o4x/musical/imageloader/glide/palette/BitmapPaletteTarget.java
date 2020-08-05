package com.o4x.musical.imageloader.glide.palette;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.ImageViewTarget;

public class BitmapPaletteTarget extends ImageViewTarget<Bitmap> {
    public BitmapPaletteTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(@Nullable Bitmap resource) {
        view.setImageBitmap(resource);
    }
}
