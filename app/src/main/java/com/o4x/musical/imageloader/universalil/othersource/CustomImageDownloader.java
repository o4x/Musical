package com.o4x.musical.imageloader.universalil.othersource;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.o4x.musical.imageloader.model.MultiImage;
import com.o4x.musical.imageloader.model.AudioFileCover;
import com.o4x.musical.imageloader.util.AudioFileCoverUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.o4x.musical.imageloader.util.MosaicUtil.getMosaic;

public class CustomImageDownloader extends BaseImageDownloader {

    private static final String PREFIX = "://";
    public static final String SCHEME_AUDIO = "audio" + PREFIX;
    public static final String SCHEME_MULTI = "multi" + PREFIX;

    public CustomImageDownloader(Context context) {
        super(context);
    }


    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra)  throws IOException {

        switch (imageUri.substring(0, imageUri.indexOf(PREFIX) + PREFIX.length())) {
            case SCHEME_AUDIO:
                return getStreamFromAudio((AudioFileCover) extra);
            case SCHEME_MULTI:
                return getStreamFromMulti((MultiImage) extra);
            default:
                return super.getStreamFromOtherSource(imageUri, extra);
        }

    }


    private InputStream getStreamFromAudio(AudioFileCover audioFileCover) {

        InputStream stream = null;
        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(audioFileCover.filePath);
            byte[] picture = retriever.getEmbeddedPicture();
            if (picture != null) {
                stream = new ByteArrayInputStream(picture);
            } else {
                stream = AudioFileCoverUtils.fallback(audioFileCover.filePath);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }

        return stream;
    }

    private InputStream getStreamFromMulti(MultiImage multiImage) {
        Log.d("MOSAIC", "load data for" + multiImage.name);
        InputStream stream = getMosaic(multiImage.albumCovers);
        return stream;
    }
}
