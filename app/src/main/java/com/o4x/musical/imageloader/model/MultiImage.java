package com.o4x.musical.imageloader.model;

import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Genre;
import com.o4x.musical.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MultiImage {
    public final String name;

    // filePath to get the image of the artist
    public final List<AlbumCover> albumCovers;

    public MultiImage(String name, final List<AlbumCover> albumCovers) {
        this.name = name;
        this.albumCovers = albumCovers;
    }

    static public MultiImage fromArtist(Artist artist) {
        final List<AlbumCover> covers = new ArrayList<>();
//        for (final Album album : artist.albums) {
//            final Song song = album.safeGetFirstSong();
//            covers.add(new AlbumCover(album.getYear(), song.data));
//        }

        return new MultiImage(artist.getName(), covers);
    }

    static public MultiImage fromGenre(Genre genre) {
        final List<AlbumCover> covers = new ArrayList<>();
        for (final Song song : genre.songs) {
            covers.add(new AlbumCover(song.albumId, song.data));
        }

        return new MultiImage(genre.name, covers);
    }

    public String toIdString() {
        StringBuilder id = new StringBuilder();
        id.append(name);
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

        MultiImage compare = (MultiImage) obj;

        try {
            return (compare.name.equals(this.name) && compare.hashCode() == this.hashCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
