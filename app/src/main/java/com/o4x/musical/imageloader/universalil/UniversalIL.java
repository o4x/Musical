package com.o4x.musical.imageloader.universalil;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

    private static final DisplayImageOptions.Builder options =
            new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true)
            .cacheInMemory(true);

    public static void initImageLoader(@NonNull Context context) {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options.build())
                .imageDownloader(new CustomImageDownloader(context))
                .build();


        ImageLoader.getInstance().init(config);

        L.writeDebugLogs(false);
        L.disableLogging();
        L.writeLogs(false);
    }

    public static void songImageLoader(
            @NonNull Song song,
            @NonNull ImageView image,
            @Nullable PaletteImageLoadingListener listener) {
        ImageLoader.getInstance().displayImage(
                MusicUtil.getMediaStoreAlbumCoverUri(song.albumId).toString(),
                image,
                options
                        .showImageOnLoading(DEFAULT_ALBUM_IMAGE)
                        .showImageOnFail(DEFAULT_ALBUM_IMAGE)
                        .showImageForEmptyUri(DEFAULT_ALBUM_IMAGE)
                        .build(),
                listener
        );
    }

    public static void audioFileImageLoader(
            @NonNull AudioFileCover audioFileCover,
            @Nullable Drawable error,
            @NonNull ImageView image,
            @Nullable PaletteImageLoadingListener listener) {

        String imageId = CustomImageDownloader.SCHEME_AUDIO + audioFileCover.hashCode();

        ImageLoader.getInstance().displayImage(
                imageId,
                image,
                options
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

        ImageLoader.getInstance().displayImage(
                imageId,
                image,
                options
                        .extraForDownloader(artistImage)
                        .showImageOnLoading(DEFAULT_ARTIST_IMAGE)
                        .showImageOnFail(DEFAULT_ARTIST_IMAGE)
                        .showImageForEmptyUri(DEFAULT_ARTIST_IMAGE)
                        .build(),
                listener
        );
    }
}
