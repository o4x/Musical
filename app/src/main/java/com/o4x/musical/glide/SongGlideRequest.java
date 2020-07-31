package com.o4x.musical.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;


import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.o4x.musical.R;
import com.o4x.musical.model.Song;
import com.o4x.musical.util.MusicUtil;



/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongGlideRequest {

    public static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.NONE;
    public static final int DEFAULT_ERROR_IMAGE = R.drawable.default_album_art;
    public static final int DEFAULT_ANIMATION = android.R.anim.fade_in;

    public static class Builder {
        final RequestManager requestManager;
        final Song song;

        public static Builder from(@NonNull RequestManager requestManager, Song song) {
            return new Builder(requestManager, song);
        }

        private Builder(@NonNull RequestManager requestManager, Song song) {
            this.requestManager = requestManager;
            this.song = song;
        }

        public BitmapBuilder asBitmap() {
            return new BitmapBuilder(this);
        }

        public RequestBuilder<Drawable> build() {
            //noinspection unchecked
            return requestManager.load(getUri(song))
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_ERROR_IMAGE)
                    .transition(DrawableTransitionOptions.withCrossFade(DEFAULT_ANIMATION))
                    .signature(createSignature(song));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public RequestBuilder<Bitmap> build() {
            //noinspection unchecked
            return builder.requestManager.asBitmap().load(getUri(builder.song.albumId))
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_ERROR_IMAGE)
                    .transition(BitmapTransitionOptions.withCrossFade(DEFAULT_ANIMATION))
                    .signature(createSignature(builder.song));
        }
    }

    public static Uri getUri(Song song) {
        return MusicUtil.getMediaStoreAlbumCoverUri(song.albumId);
    }

    public static Uri getUri(int albumId) {
        return MusicUtil.getMediaStoreAlbumCoverUri(albumId);
    }

    public static Key createSignature(Song song) {
        return new MediaStoreSignature("", song.dateModified, 0);
    }
}
