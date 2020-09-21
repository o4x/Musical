package com.o4x.musical.imageloader.universalil.palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.o4x.musical.R;
import com.o4x.musical.util.PhonographColorUtil;

import code.name.monkey.appthemehelper.util.ATHUtil;

public abstract class PaletteImageLoadingListener extends AbsImageLoadingListener {

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        super.onLoadingFailed(imageUri, view, failReason);
        if (onFailedBitmap == null) {
            onColorReady(getDefaultFooterColor(view.getContext()));
        } else {
            onColorReady(
                    PhonographColorUtil.getColor(Palette.from(onFailedBitmap).generate(),
                            getDefaultFooterColor(view.getContext()))
            );
        }
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        super.onLoadingCancelled(imageUri, view);
        onColorReady(getDefaultFooterColor(view.getContext()));
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        super.onLoadingComplete(imageUri, view, loadedImage);
        onColorReady(
                PhonographColorUtil.getColor(Palette.from(loadedImage).generate(),
                getDefaultFooterColor(view.getContext()))
        );
    }

    protected int getDefaultFooterColor(@NonNull Context context) {
        return ATHUtil.INSTANCE.resolveColor(context, R.attr.defaultFooterColor);
    }

    protected int getAlbumArtistFooterColor(@NonNull Context context) {
        return ATHUtil.INSTANCE.resolveColor(context, R.attr.cardBackgroundColor);
    }

    public abstract void onColorReady(int color);
}
