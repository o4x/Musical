package com.o4x.musical.imageloader.glide.targets;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.o4x.musical.helper.MyPalette;

public abstract class PaletteTargetListener extends AbsPaletteTargetListener {

    private AsyncTask<Bitmap, Void, Palette> paletteAsyncTask;

    @Override
    public void onResourceReady(@Nullable Bitmap resource) {
        if (paletteAsyncTask != null) paletteAsyncTask.cancel(false);

        if (resource == null) {
            onColorReady(new MyPalette(null));
        } else {
            if (isSync) {
                onColorReady(
                        new MyPalette(
                                Palette.from(resource).generate()
                        )
                );
            } else {
                paletteAsyncTask = Palette.from(resource).generate(
                        palette -> onColorReady(new MyPalette(palette))
                );
            }
        }
    }

    public abstract void onColorReady(MyPalette colors);
}

