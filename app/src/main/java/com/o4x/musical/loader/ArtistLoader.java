package com.o4x.musical.loader;

import android.content.Context;
import android.provider.MediaStore.Audio.AudioColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.o4x.musical.model.Album;
import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Song;
import com.o4x.musical.repository.RealAlbumRepository;
import com.o4x.musical.repository.RealSongRepository;
import com.o4x.musical.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistLoader {
    public static String getSongLoaderSortOrder(Context context) {
        return PreferenceUtil.getArtistSortOrder() + ", " + PreferenceUtil.getArtistAlbumSortOrder() + ", " + PreferenceUtil.getAlbumSongSortOrder();
    }

    @NonNull
    public static List<Artist> getAllArtists(@NonNull final Context context) {
        RealSongRepository songRepo = new RealSongRepository(context);
        List<Song> songs = songRepo.songs(songRepo.makeSongCursor(
                null,
                null,
                getSongLoaderSortOrder(context))
        );
        return splitIntoArtists(new RealAlbumRepository(songRepo).splitIntoAlbums(songs));
    }

    @NonNull
    public static List<Artist> getArtists(@NonNull final Context context, String query) {
        RealSongRepository songRepo = new RealSongRepository(context);
        List<Song> songs = songRepo.songs(songRepo.makeSongCursor(
                AudioColumns.ARTIST + " LIKE ?",
                new String[]{"%" + query + "%"},
                getSongLoaderSortOrder(context))
        );
        return splitIntoArtists(new RealAlbumRepository(songRepo).splitIntoAlbums(songs));
    }

    @NonNull
    public static Artist getArtist(@NonNull final Context context, long artistId) {
        RealSongRepository songRepo = new RealSongRepository(context);
        List<Song> songs = songRepo.songs(songRepo.makeSongCursor(
                AudioColumns.ARTIST_ID + "=?",
                new String[]{String.valueOf(artistId)},
                getSongLoaderSortOrder(context))
        );
        return new Artist(artistId, new RealAlbumRepository(songRepo).splitIntoAlbums(songs));
    }

    @NonNull
    public static List<Artist> splitIntoArtists(@Nullable final List<Album> albums) {
        List<Artist> artists = new ArrayList<>();
        if (albums != null) {
            for (Album album : albums) {
                getOrCreateArtist(artists, album.getArtistId()).getAlbums().add(album);
            }
        }
        return artists;
    }

    private static Artist getOrCreateArtist(List<Artist> artists, long artistId) {
        for (Artist artist : artists) {
            if (!artist.getAlbums().isEmpty() && !artist.getAlbums().get(0).getSongs().isEmpty() && artist.getAlbums().get(0).getSongs().get(0).getArtistId() == artistId) {
                return artist;
            }
        }
        Artist artist = Artist.Companion.getEmpty();
        artists.add(artist);
        return artist;
    }
}
