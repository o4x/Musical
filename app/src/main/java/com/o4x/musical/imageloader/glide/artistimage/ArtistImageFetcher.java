package com.o4x.musical.imageloader.glide.artistimage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.o4x.musical.imageloader.model.AlbumCover;
import com.o4x.musical.imageloader.model.ArtistImage;
import com.o4x.musical.util.ImageUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.o4x.musical.imageloader.util.AudioFileCoverUtils.fallback;
import static com.o4x.musical.imageloader.util.MosaicUtil.getMosaic;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImageFetcher implements DataFetcher<InputStream> {

    private final ArtistImage model;

    private InputStream stream;

    public ArtistImageFetcher(final ArtistImage model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        Log.d("MOSAIC", "load data for" + model.artistName);
        stream = getMosaic(model.albumCovers);
        callback.onDataReady(stream);
    }

    @Override
    public void cleanup() {
        // already cleaned up in loadData and ByteArrayInputStream will be GC'd
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                // can't do much about it
            }
        }
    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }



    public String getId() {
        Log.d("MOSAIC", "get id for" + model.artistName);
        // never return NULL here!
        // this id is used to determine whether the image is already cached
        // we use the artist name as well as the album years + file paths
        return model.toIdString();
    }

    @Override
    public int hashCode() {
        return Math.abs(getId().hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        ArtistImageFetcher compare = (ArtistImageFetcher) obj;

        try {
            return (compare.getId().equals(this.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
