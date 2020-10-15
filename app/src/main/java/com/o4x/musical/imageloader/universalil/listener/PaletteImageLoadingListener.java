package com.o4x.musical.imageloader.universalil.listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.loader.content.AsyncTaskLoader;
import androidx.palette.graphics.Palette;
import androidx.palette.graphics.Target;

import com.o4x.musical.R;
import com.o4x.musical.extensions.ColorExtKt;
import com.o4x.musical.util.PhonographColorUtil;

import code.name.monkey.appthemehelper.util.ATHUtil;

public abstract class PaletteImageLoadingListener extends AbsImageLoadingListener {

    private AsyncTaskLoader<Integer> paletteTask;

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        super.onLoadingCancelled(imageUri, view);
        colorReady(null);
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        super.onLoadingComplete(imageUri, view, loadedImage);
        colorReady(loadedImage);
    }

    private void colorReady(Bitmap image) {
        if (paletteTask != null) {
            paletteTask.cancelLoad();
        }

        paletteTask = new AsyncTaskLoader<Integer>(context) {
            @Override
            public Integer loadInBackground() {
                if (image == null) {
                    return getDefaultFooterColor(context);
                } else {
                    return PhonographColorUtil.getColor(
                            Palette.from(image).generate(),
                            getDefaultFooterColor(context)
                    );
                }
            }

            @Override
            public void deliverResult(Integer data) {
                super.deliverResult(data);
                onColorReady(data);
            }
        };

        paletteTask.forceLoad();
    }

    @Override
    public void onFailedBitmapReady(Bitmap failedBitmap) {
        super.onFailedBitmapReady(failedBitmap);
        colorReady(failedBitmap);
    }

    public abstract void onColorReady(int color);

    protected int getDefaultFooterColor(@NonNull Context context) {
        return ATHUtil.INSTANCE.resolveColor(context, R.attr.defaultFooterColor);
    }

    protected int getAlbumArtistFooterColor(@NonNull Context context) {
        return ATHUtil.INSTANCE.resolveColor(context, R.attr.cardBackgroundColor);
    }
}
