package com.o4x.musical.util;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.o4x.musical.App;
import com.o4x.musical.imageloader.universalil.loader.UniversalIL;
import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Genre;
import com.o4x.musical.model.Playlist;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class CustomImageUtil {

    private static final String FOLDER_NAME = "/images/";

    private final long id;
    private final String name;
    private final Type type;

    public CustomImageUtil(long id, String name, Type type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public CustomImageUtil(Artist artist) {
        this.id = artist.getId();
        this.name = artist.getName();
        this.type = Type.ARTIST;
    }

    public CustomImageUtil(Genre genre) {
        this.id = genre.getId();
        this.name = genre.getName();
        this.type = Type.GENRE;
    }

    public CustomImageUtil(Playlist playlist) {
        this.id = playlist.getId();
        this.name = playlist.getName();
        this.type = Type.PLAYLIST;
    }

    public void setCustomImage(Uri uri) {
        Glide.with(App.Companion.getContext())
                .asBitmap()
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        setCustomImage(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                    }
                });
    }

    @SuppressLint("StaticFieldLeak")
    public void setCustomImage(Bitmap bitmap) {
        if (bitmap == null) return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                File file = getFile();

                boolean succesful = false;
                try {
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                    succesful = ImageUtil.resizeBitmap(bitmap, 2048).compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.close();
                } catch (IOException e) {
                    Toast.makeText(App.Companion.getContext(), e.toString(), Toast.LENGTH_LONG).show();
                }

                if (succesful) {
                    // Remove cache from universal image loader for reload image
                    // For glide we don't need to remove cache, it's work with Signature
                    UniversalIL.Companion.removeFromCache(getPath());
                    notifyChange();
                }
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void resetCustomImage() {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("ApplySharedPref")
            @Override
            protected Void doInBackground(Void... params) {
                notifyChange();
                File file = getFile();
                if (!file.exists()) {
                    return null;
                } else {
                    // Remove caches from UIL just for optimize memory
                    UniversalIL.Companion.removeFromCache(getPath());
                    file.delete();
                }
                return null;
            }
        }.execute();
    }

    private void notifyChange() {
        // trigger media store changed to force image reload
        switch (type) {
            case ARTIST:
                App.Companion.getContext().getContentResolver()
                        .notifyChange(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null);
                break;
            case GENRE:
                App.Companion.getContext().getContentResolver()
                        .notifyChange(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, null);
                break;
            case PLAYLIST:
                App.Companion.getContext().getContentResolver()
                        .notifyChange(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null);
                break;
        }
    }

    public boolean hasCustomImage() {
        return getFile().exists();
    }

    private String getFileName() {
        String mName = name;
        if (mName == null)
            mName = "";
        // replace everything that is not a letter or a number with _
        mName = mName.replaceAll("[^a-zA-Z0-9]", "_");
        return String.format(Locale.US, "%s_%d.jpeg", mName, id);
    }

    public File getFile() {
        File dir = new File(App.Companion.getContext().getFilesDir(), FOLDER_NAME + type.name());
        if (!dir.exists()) {
            if (!dir.mkdirs()) { // create the folder
                return null;
            }
        }
        return new File(dir, getFileName());
    }

    public String getPath() {
        return Uri.fromFile(getFile()).toString();
    }

    public enum Type {
        ARTIST,
        GENRE,
        PLAYLIST
    }
}
