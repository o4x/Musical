package com.o4x.musical.imageloader.glide.palette;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.request.transition.Transition;
import com.o4x.musical.R;
import com.o4x.musical.util.PhonographColorUtil;

import code.name.monkey.appthemehelper.util.ATHUtil;

public abstract class PhonographColoredTarget extends BitmapPaletteTarget {
    public PhonographColoredTarget(ImageView view) {
        super(view);
    }

    @Override
    public void onLoadFailed(Drawable errorDrawable) {
        super.onLoadFailed(errorDrawable);
        onColorReady(getDefaultFooterColor());
    }

    @Override
    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
        super.onResourceReady(resource, transition);
        onColorReady(PhonographColorUtil.getColor(Palette.from(resource).generate(), getDefaultFooterColor()));
    }

    protected int getDefaultFooterColor() {
        return ATHUtil.INSTANCE.resolveColor(getView().getContext(), R.attr.defaultFooterColor);
    }

    protected int getAlbumArtistFooterColor() {
        return ATHUtil.INSTANCE.resolveColor(getView().getContext(), R.attr.cardBackgroundColor);
    }

    public abstract void onColorReady(int color);
}

