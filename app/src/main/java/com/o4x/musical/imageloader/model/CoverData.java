package com.o4x.musical.imageloader.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.o4x.musical.model.Song;
import com.o4x.musical.util.CoverUtil;
import com.o4x.musical.views.SquareImageView;

import static com.o4x.musical.views.SquareImageView.createSquareCoverWithText;

public class CoverData {

    @Nullable
    public Context context;
    @Nullable
    public ImageView image;
    public int size = SquareImageView.DEFAULT_SIZE;

    public final long id;
    public final String text;

    public CoverData(long id, String text) {
        this.id = id;
        this.text = text;
    }

    public Bitmap create(@NonNull Context context) {
        return createSquareCoverWithText(
                context, text, id, size);
    }

    public static CoverData from(Song song) {
        return new CoverData(song.getAlbumId(), song.getAlbumName());
    }

    public static CoverData from(AudioFileCover audioFileCover) {
        return new CoverData(audioFileCover.hashCode(), audioFileCover.title);
    }

    public static CoverData from(MultiImage multiImage) {
        return new CoverData(multiImage.id, multiImage.name);
    }

    public static CoverData from(String url, String name) {
        return new CoverData(url.hashCode(), name);
    }
}
