package com.o4x.musical.loader;

import android.content.Context;
import android.provider.MediaStore.Audio.AudioColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.o4x.musical.model.Album;
import com.o4x.musical.model.Song;
import com.o4x.musical.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumLoader {

    public static String getSongLoaderSortOrder(Context context) {
        return PreferenceUtil.getAlbumSortOrder() + ", " + PreferenceUtil.getAlbumSongSortOrder();
    }

    @NonNull
    public static List<Album> getAllAlbums(@NonNull final Context context) {
        List<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                null,
                null,
                getSongLoaderSortOrder(context))
        );
        return splitIntoAlbums(songs);
    }

    @NonNull
    public static List<Album> getAlbums(@NonNull final Context context, String query) {
        List<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                AudioColumns.ALBUM + " LIKE ?",
                new String[]{"%" + query + "%"},
                getSongLoaderSortOrder(context))
        );
        return splitIntoAlbums(songs);
    }

    @NonNull
    public static Album getAlbum(@NonNull final Context context, long albumId) {
        List<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(context, AudioColumns.ALBUM_ID + "=?", new String[]{String.valueOf(albumId)}, getSongLoaderSortOrder(context)));
        Album album = new Album(albumId, songs);
        sortSongsByTrackNumber(album);
        return album;
    }

    @NonNull
    public static List<Album> splitIntoAlbums(@Nullable final List<Song> songs) {
        List<Album> albums = new ArrayList<>();
        if (songs != null) {
            for (Song song : songs) {
                getOrCreateAlbum(albums, song.getAlbumId()).getSongs().add(song);
            }
        }
        for (Album album : albums) {
            sortSongsByTrackNumber(album);
        }
        return albums;
    }

    private static Album getOrCreateAlbum(List<Album> albums, long albumId) {
        for (Album album : albums) {
            if (!album.getSongs().isEmpty() && album.getSongs().get(0).getAlbumId() == albumId) {
                return album;
            }
        }
        Album album = Album.Companion.getEmpty();
        albums.add(album);
        return album;
    }

    private static void sortSongsByTrackNumber(Album album) {
        Collections.sort(album.getSongs(), (o1, o2) -> o1.getTrackNumber() - o2.getTrackNumber());
    }
}
