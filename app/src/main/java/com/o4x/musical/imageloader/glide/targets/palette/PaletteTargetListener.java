package com.o4x.musical.imageloader.glide.targets.palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.o4x.musical.helper.MyPalette;

import org.jetbrains.annotations.NotNull;

public abstract class PaletteTargetListener extends AbsPaletteTargetListener {

    private AsyncTask<Bitmap, Void, Palette> paletteAsyncTask;
    private final Context context;

    protected PaletteTargetListener(Context context) {
        this.context = context;
    }

    @Override
    public void onResourceReady(@Nullable Bitmap resource) {
        if (paletteAsyncTask != null) paletteAsyncTask.cancel(false);

        if (resource == null) {
            onColorReady(new MyPalette(context, null), null);
        } else {
            final Palette.Builder paletteBuilder =
                    Palette.from(resource);
            if (isSync) {
                onColorReady(
                        new MyPalette(
                                context,
                                paletteBuilder.generate()
                        ), resource
                );
            } else {
                paletteAsyncTask = paletteBuilder.generate(
                        palette -> onColorReady(new MyPalette(context, palette), resource)
                );
            }
        }
    }

    public abstract void onColorReady(@NotNull MyPalette colors, @Nullable Bitmap resource);
}

