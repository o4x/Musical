package com.o4x.musical.imageloader.universalil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.L;
import com.o4x.musical.R;
import com.o4x.musical.imageloader.model.ArtistImage;
import com.o4x.musical.imageloader.model.AudioFileCover;
import com.o4x.musical.imageloader.universalil.othersource.CustomImageDownloader;
import com.o4x.musical.imageloader.universalil.palette.PaletteImageLoadingListener;
import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Song;
import com.o4x.musical.util.MusicUtil;

public class UniversalIL {

    private static final int DEFAULT_ARTIST_IMAGE = R.drawable.default_artist_image;
    private static final int DEFAULT_ALBUM_IMAGE = R.drawable.default_album_art;


    private static DisplayImageOptions.Builder getOptions() {
        return new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .cacheInMemory(true);
    }

    private static final DisplayImageOptions songOptions =
            getOptions()
                    .showImageOnLoading(DEFAULT_ALBUM_IMAGE)
                    .showImageOnFail(DEFAULT_ALBUM_IMAGE)
                    .showImageForEmptyUri(DEFAULT_ALBUM_IMAGE)
                    .build();


    private static final DisplayImageOptions onlineOptions =
            new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .showImageOnLoading(DEFAULT_ALBUM_IMAGE)
                    .showImageOnFail(DEFAULT_ALBUM_IMAGE)
                    .showImageForEmptyUri(DEFAULT_ALBUM_IMAGE)
                    .build();


    private static ImageLoader imageLoader;

    public static void initImageLoader(@NonNull Context context) {

        imageLoader = ImageLoader.getInstance();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(getOptions().build())
                .diskCacheSize(1024 * 1024 * 100 /* 100MB */)
                .memoryCacheSize(1024 * 1024 * 300 /* 300MB */)
                .imageDownloader(new CustomImageDownloader(context))
                .build();


        imageLoader.init(config);

        L.writeDebugLogs(false);
        L.writeLogs(false);
    }

    public static ImageLoader getImageLoader() {
        return imageLoader;
    }

    public static void songImageLoader(
            @NonNull Song song,
            @NonNull ImageView image,
            @Nullable PaletteImageLoadingListener listener) {
        imageLoader.displayImage(
                MusicUtil.getMediaStoreAlbumCoverUri(song.albumId).toString(),
                image,
                songOptions,
                listener
        );
    }

    public static void audioFileImageLoader(
            @NonNull AudioFileCover audioFileCover,
            @Nullable Drawable error,
            @NonNull ImageView image,
            @Nullable PaletteImageLoadingListener listener) {

        String imageId = CustomImageDownloader.SCHEME_AUDIO + audioFileCover.filePath;

        imageLoader.displayImage(
                imageId,
                image,
                getOptions()
                        .extraForDownloader(audioFileCover)
                        .showImageOnLoading(error)
                        .showImageOnFail(error)
                        .showImageForEmptyUri(error)
                        .build(),
                listener
        );
    }

    public static void artistImageLoader(
            @NonNull Artist artist,
            @NonNull ImageView image,
            @Nullable PaletteImageLoadingListener listener) {


        ArtistImage artistImage = ArtistImage.fromArtist(artist);

        String imageId = CustomImageDownloader.SCHEME_ARTIST + artistImage.hashCode();

        imageLoader.displayImage(
                imageId,
                image,
                getOptions()
                        .extraForDownloader(artistImage)
                        .showImageOnLoading(DEFAULT_ARTIST_IMAGE)
                        .showImageOnFail(DEFAULT_ARTIST_IMAGE)
                        .showImageForEmptyUri(DEFAULT_ARTIST_IMAGE)
                        .build(),
                listener
        );
    }

    public static void onlineAlbumImageLoader(
            @NonNull String url,
            @NonNull ImageView image,
            @Nullable PaletteImageLoadingListener listener) {
        if (!TextUtils.isEmpty(url) && url.trim().length() > 0)
        imageLoader.displayImage(
                url,
                image,
                onlineOptions,
                listener
        );
    }
}
