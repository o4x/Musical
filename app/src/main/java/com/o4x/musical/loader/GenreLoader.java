package com.o4x.musical.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Genres;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.o4x.musical.Constants;
import com.o4x.musical.model.Genre;
import com.o4x.musical.model.Song;
import com.o4x.musical.repository.RealSongRepository;
import com.o4x.musical.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import static com.o4x.musical.Constants.IS_MUSIC;

public class GenreLoader {

    @NonNull
    public static List<Genre> getAllGenres(@NonNull final Context context) {
        return getGenresFromCursor(context, makeGenreCursor(context));
    }

    @NonNull
    public static List<Song> getSongs(@NonNull final Context context, final long genreId) {
        return new RealSongRepository(context).songs(makeGenreSongCursor(context, genreId));
    }

    @NonNull
    private static List<Genre> getGenresFromCursor(@NonNull final Context context, @Nullable final Cursor cursor) {
        final List<Genre> genres = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Genre genre = getGenreFromCursor(context, cursor);
                    if (genre.getSongCount() > 0) {
                        genres.add(genre);
                    } else {
                        // try to remove the empty genre from the media store
                        try {
                            context.getContentResolver().delete(Genres.EXTERNAL_CONTENT_URI, Genres._ID + " == " + genre.getId(), null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // nothing we can do then
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return genres;
    }

    @NonNull
    private static Genre getGenreFromCursor(@NonNull final Context context, @NonNull final Cursor cursor) {
        final long id = cursor.getLong(0);
        final String name = cursor.getString(1);
        final List<Song> songs = getSongs(context, id);
        return new Genre(id, name, songs, songs.size());
    }

    @Nullable
    private static Cursor makeGenreSongCursor(@NonNull final Context context, long genreId) {
        try {
            return context.getContentResolver().query(
                    Genres.Members.getContentUri("external", genreId),
                    Constants.getBaseProjection(), IS_MUSIC, null, PreferenceUtil.getSongSortOrder());
        } catch (SecurityException e) {
            return null;
        }
    }

    @Nullable
    private static Cursor makeGenreCursor(@NonNull final Context context) {
        final String[] projection = new String[]{
                Genres._ID,
                Genres.NAME
        };

        try {
            return context.getContentResolver().query(
                    Genres.EXTERNAL_CONTENT_URI,
                    projection, null, null, PreferenceUtil.getGenreSortOrder());
        } catch (SecurityException e) {
            return null;
        }
    }
}
