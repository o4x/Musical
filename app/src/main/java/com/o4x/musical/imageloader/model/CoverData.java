package com.o4x.musical.imageloader.model;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.o4x.musical.util.CoverUtil;

public class CoverData {

    @Nullable
    public Context context;
    @Nullable
    public ImageView image;
    public int size = CoverUtil.DEFAULT_SIZE;

    public final long id;
    public final String text;

    public CoverData(long id, String text) {
        this.id = id;
        this.text = text;
    }

    public void setImage(@Nullable ImageView image) {
        this.image = image;
    }

    public void setContext(@Nullable Context context) {
        this.context = context;
    }
}
