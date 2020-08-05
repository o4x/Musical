package com.o4x.musical.imageloader.model;

import com.o4x.musical.model.Album;
import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImage {
    public final String artistName;

    // filePath to get the image of the artist
    public final List<AlbumCover> albumCovers;

    public ArtistImage(String artistName, final List<AlbumCover> albumCovers) {
        this.artistName = artistName;
        this.albumCovers = albumCovers;
    }

    static public ArtistImage fromArtist(Artist artist) {
        final List<AlbumCover> covers = new ArrayList<>();
        for (final Album album : artist.albums) {
            final Song song = album.safeGetFirstSong();
            covers.add(new AlbumCover(album.getYear(), song.data));
        }

        return new ArtistImage(artist.getName(), covers);
    }

    public String toIdString() {
        StringBuilder id = new StringBuilder();
        id.append(artistName);
        for (AlbumCover albumCover: albumCovers) {
            id.append(albumCover.getYear()).append(albumCover.getFilePath());
        }
        return id.toString();
    }

    @Override
    public int hashCode() {
        return Math.abs(toIdString().hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        ArtistImage compare = (ArtistImage) obj;

        try {
            return (compare.artistName.equals(this.artistName) && compare.hashCode() == this.hashCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
