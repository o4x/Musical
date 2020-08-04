package com.o4x.musical.universalIL;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.L;
import com.o4x.musical.model.Song;
import com.o4x.musical.universalIL.palette.PaletteImageLoadingListener;
import com.o4x.musical.util.MusicUtil;

public class BaseUniversalIL {

    static public void initImageLoader(@NonNull Context context) {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);

        L.writeDebugLogs(false);
        L.disableLogging();
        L.writeLogs(false);
    }

    static public void songImageLoader(@NonNull Song song, @NonNull ImageView image, @Nullable PaletteImageLoadingListener listener) {
        ImageLoader.getInstance().displayImage(
                MusicUtil.getMediaStoreAlbumCoverUri(song.albumId).toString(),
                image,
                listener
        );
    }
}
