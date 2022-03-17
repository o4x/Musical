package github.o4x.musical.imageloader.glide.module.audiocover;

import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import github.o4x.musical.imageloader.model.AudioFileCover;
import github.o4x.musical.imageloader.util.AudioFileCoverUtils;

public class AudioFileCoverFetcher implements DataFetcher<InputStream> {
    private final AudioFileCover model;

    private InputStream stream;

    public AudioFileCoverFetcher(AudioFileCover model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(model.filePath);
            byte[] picture = retriever.getEmbeddedPicture();
            if (picture != null) {
                stream = new ByteArrayInputStream(picture);
            } else {
                stream = AudioFileCoverUtils.fallback(model.filePath);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
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
}
