package com.o4x.musical.imageloader.glide.targets;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.o4x.musical.App;
import com.o4x.musical.R;
import com.o4x.musical.util.PhonographColorUtil;

import code.name.monkey.appthemehelper.util.ATHUtil;

public abstract class PaletteColoredTargetListener extends PaletteTargetListener {

    private AsyncTask<Bitmap, Void, Palette> paletteAsyncTask;

    @Override
    public void onResourceReady(@Nullable Bitmap resource) {
        if (paletteAsyncTask != null) paletteAsyncTask.cancel(false);

        if (resource == null) {
            onColorReady(getDefaultFooterColor());
        } else {
            if (isSync) {
                onColorReady(
                        PhonographColorUtil.getColor(
                                Palette.from(resource).generate(), getDefaultFooterColor())
                );
            } else {
                paletteAsyncTask = Palette.from(resource).generate(
                        palette -> onColorReady(
                                PhonographColorUtil.getColor(palette, getDefaultFooterColor())
                        )
                );
            }
        }
    }


    protected int getDefaultFooterColor() {
        return ATHUtil.INSTANCE.resolveColor(App.Companion.getContext(), R.attr.defaultFooterColor);
    }

    protected int getAlbumArtistFooterColor() {
        return ATHUtil.INSTANCE.resolveColor(App.Companion.getContext(), R.attr.cardBackgroundColor);
    }

    public abstract void onColorReady(int color);
}

