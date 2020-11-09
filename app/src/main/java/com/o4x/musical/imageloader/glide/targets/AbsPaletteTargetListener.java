package com.o4x.musical.imageloader.glide.targets;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.o4x.musical.imageloader.model.CoverData;

public class AbsPaletteTargetListener {

    public CoverData coverData;
    public boolean isSync = false;

    public void onResourceReady(@Nullable Bitmap resource) {}
}