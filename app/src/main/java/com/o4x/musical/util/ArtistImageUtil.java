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
import com.o4x.musical.model.Artist;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class ArtistImageUtil {

    private static final String FOLDER_NAME = "/artist_images/";

    @SuppressLint("StaticFieldLeak")
    public static void setCustomArtistImage(final Artist artist, Uri uri) {
        Glide.with(App.getInstance())
                .asBitmap()
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        new AsyncTask<Void, Void, Void>() {
                            @SuppressLint("ApplySharedPref")
                            @Override
                            protected Void doInBackground(Void... params) {
                                File dir = new File(App.getInstance().getFilesDir(), FOLDER_NAME);
                                if (!dir.exists()) {
                                    if (!dir.mkdirs()) { // create the folder
                                        return null;
                                    }
                                }
                                File file = new File(dir, getFileName(artist));

                                boolean succesful = false;
                                try {
                                    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                                    succesful = ImageUtil.resizeBitmap(resource, 2048).compress(Bitmap.CompressFormat.JPEG, 100, os);
                                    os.close();
                                } catch (IOException e) {
                                    Toast.makeText(App.getInstance(), e.toString(), Toast.LENGTH_LONG).show();
                                }

                                if (succesful) {
                                    ArtistSignatureUtil.getInstance(App.getInstance()).updateArtistSignature(artist.getName());
                                    App.getInstance().getContentResolver().notifyChange(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null); // trigger media store changed to force artist image reload
                                }
                                return null;
                            }
                        }.execute();
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
    public static void resetCustomArtistImage(final Artist artist) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("ApplySharedPref")
            @Override
            protected Void doInBackground(Void... params) {
                ArtistSignatureUtil.getInstance(App.getInstance()).updateArtistSignature(artist.getName());
                App.getInstance().getContentResolver().notifyChange(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null); // trigger media store changed to force artist image reload

                File file = getFile(artist);
                if (!file.exists()) {
                    return null;
                } else {
                    file.delete();
                }
                return null;
            }
        }.execute();
    }


    public static boolean hasCustomArtistImage(Artist artist) {
        return getFile(artist).exists();
    }

    private static String getFileName(Artist artist) {
        String artistName = artist.getName();
        if (artistName == null)
            artistName = "";
        // replace everything that is not a letter or a number with _
        artistName = artistName.replaceAll("[^a-zA-Z0-9]", "_");
        return String.format(Locale.US, "%s_%d.jpeg", artistName, artist.getId());
    }

    public static File getFile(Artist artist) {
        File dir = new File(App.getInstance().getFilesDir(), FOLDER_NAME);
        return new File(dir, getFileName(artist));
    }

    public static String getPath(Artist artist) {
        return Uri.fromFile(ArtistImageUtil.getFile(artist)).toString();
    }
}
