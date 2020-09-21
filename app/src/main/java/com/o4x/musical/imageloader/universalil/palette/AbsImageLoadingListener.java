package com.o4x.musical.imageloader.universalil.palette;

import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public abstract class AbsImageLoadingListener extends SimpleImageLoadingListener {

    protected Bitmap onFailedBitmap;

    public void setOnFailedBitmap(Bitmap onFailedBitmap) {
        this.onFailedBitmap = onFailedBitmap;
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        super.onLoadingFailed(imageUri, view, failReason);
        if (view == null) return;
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        super.onLoadingCancelled(imageUri, view);
        if (view == null) return;
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        super.onLoadingComplete(imageUri, view, loadedImage);
        if (view == null) return;
    }
}
