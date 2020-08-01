package com.o4x.musical.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.Target;
import com.o4x.musical.App;
import com.o4x.musical.R;
import com.o4x.musical.glide.artistimage.AlbumCover;
import com.o4x.musical.glide.artistimage.ArtistImage;
import com.o4x.musical.model.Album;
import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Song;
import com.o4x.musical.util.ArtistSignatureUtil;
import com.o4x.musical.util.CustomArtistImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistGlideRequest {

    private static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.ALL;
    private static final int DEFAULT_ERROR_IMAGE = R.drawable.default_artist_image;
    public static final int DEFAULT_ANIMATION = 300;

    public static class Builder {
        final RequestManager requestManager;
        final Artist artist;
        boolean noCustomImage;

        public static Builder from(@NonNull RequestManager requestManager, Artist artist) {
            return new Builder(requestManager, artist);
        }

        private Builder(@NonNull RequestManager requestManager, Artist artist) {
            this.requestManager = requestManager;
            this.artist = artist;
        }

        public BitmapBuilder asBitmap() {
            return new BitmapBuilder(this);
        }

        public Builder noCustomImage(boolean noCustomImage) {
            this.noCustomImage = noCustomImage;
            return this;
        }

        public RequestBuilder<Drawable> build() {
            //noinspection unchecked
            return createBaseRequest(requestManager, artist, noCustomImage)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_ERROR_IMAGE)
                    .transition(DrawableTransitionOptions.withCrossFade(DEFAULT_ANIMATION))
                    .priority(Priority.LOW)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(artist));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public RequestBuilder<Bitmap> build() {
            //noinspection unchecked
            return createBaseRequestAsBitmap(builder.requestManager, builder.artist, builder.noCustomImage)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .error(DEFAULT_ERROR_IMAGE)
                    .transition(BitmapTransitionOptions.withCrossFade(DEFAULT_ANIMATION))
                    .priority(Priority.LOW)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(builder.artist));
        }
    }

    public static RequestBuilder<Drawable> createBaseRequest(RequestManager requestManager, Artist artist, boolean noCustomImage) {
        boolean hasCustomImage = CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist);
        if (noCustomImage || !hasCustomImage) {
            return requestManager.load(getUri(artist, noCustomImage));
        } else {
            return requestManager.load(getUri(artist, false));
        }
    }

    public static RequestBuilder<Bitmap> createBaseRequestAsBitmap(RequestManager requestManager, Artist artist, boolean noCustomImage) {
        boolean hasCustomImage = CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist);
        if (noCustomImage || !hasCustomImage) {
            return requestManager.asBitmap().load(getUri(artist, noCustomImage));
        } else {
            return requestManager.asBitmap().load(getUri(artist, false));
        }
    }

    public static Object getUri(Artist artist, boolean noCustomImage) {
        boolean hasCustomImage = CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist);
        if (noCustomImage || !hasCustomImage) {
            final List<AlbumCover> songs = new ArrayList<>();
            for (final Album album : artist.albums) {
                final Song song = album.safeGetFirstSong();
                songs.add(new AlbumCover(album.getYear(), song.data));
            }
            return new ArtistImage(artist.getName(), songs);
        } else {
            return CustomArtistImageUtil.getFile(artist);
        }
    }

    private static Key createSignature(Artist artist) {
        return ArtistSignatureUtil.getInstance(App.getInstance()).getArtistSignature(artist.getName());
    }
}
