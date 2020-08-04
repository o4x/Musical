package com.o4x.musical.universalIL.palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.o4x.musical.R;
import com.o4x.musical.util.PhonographColorUtil;

public abstract class PaletteImageLoadingListener extends SimpleImageLoadingListener {

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        super.onLoadingFailed(imageUri, view, failReason);
        onColorReady(getDefaultFooterColor(view.getContext()));
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
        return ATHUtil.resolveColor(context, R.attr.defaultFooterColor);
    }

    protected int getAlbumArtistFooterColor(@NonNull Context context) {
        return ATHUtil.resolveColor(context, R.attr.cardBackgroundColor);
    }

    public abstract void onColorReady(int color);
}
