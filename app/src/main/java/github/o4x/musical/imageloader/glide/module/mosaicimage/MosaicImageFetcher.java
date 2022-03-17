package github.o4x.musical.imageloader.glide.module.mosaicimage;

import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import github.o4x.musical.imageloader.model.MultiImage;

import java.io.IOException;
import java.io.InputStream;

import static github.o4x.musical.imageloader.util.MosaicUtil.getMosaic;

public class MosaicImageFetcher implements DataFetcher<InputStream> {

    private final MultiImage model;

    private InputStream stream;

    public MosaicImageFetcher(final MultiImage model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        Log.d("MOSAIC", "load data for" + model.name);
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
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                // can't do much about it
            }
        }
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
        Log.d("MOSAIC", "get id for" + model.name);
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

        MosaicImageFetcher compare = (MosaicImageFetcher) obj;

        try {
            return (compare.getId().equals(this.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
