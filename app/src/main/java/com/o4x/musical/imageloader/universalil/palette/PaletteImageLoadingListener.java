package com.o4x.musical.imageloader.universalil.palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.o4x.musical.R;
import com.o4x.musical.extensions.ColorExtKt;
import com.o4x.musical.util.PhonographColorUtil;

import code.name.monkey.appthemehelper.util.ATHUtil;

public abstract class PaletteImageLoadingListener extends AbsImageLoadingListener {

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        super.onLoadingFailed(imageUri, view, failReason);
        if (onFailedBitmap == null) {
            colorReady(
                    view.getContext(),
                    getDefaultFooterColor(view.getContext())
            );
        } else {
            colorReady(
                    view.getContext(),
                    PhonographColorUtil.getColor(
                            Palette.from(onFailedBitmap).generate(),
                            getDefaultFooterColor(view.getContext())
                    )
            );
        }
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        super.onLoadingCancelled(imageUri, view);
        colorReady(view.getContext(), getDefaultFooterColor(view.getContext()));
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        super.onLoadingComplete(imageUri, view, loadedImage);
        colorReady(
                view.getContext(),
                PhonographColorUtil.getColor(
                        Palette.from(loadedImage).generate(),
                        getDefaultFooterColor(view.getContext())
                )
        );
    }

    protected int getDefaultFooterColor(@NonNull Context context) {
        return ATHUtil.INSTANCE.resolveColor(context, R.attr.defaultFooterColor);
    }

    protected int getAlbumArtistFooterColor(@NonNull Context context) {
        return ATHUtil.INSTANCE.resolveColor(context, R.attr.cardBackgroundColor);
    }

    private void colorReady(Context context, int color) {
        onColorReady(
                ColorUtils.blendARGB(
                        color,
                        ColorExtKt.surfaceColor(context),
                        .5f
                )
        );
    }

    public abstract void onColorReady(int color);
}
