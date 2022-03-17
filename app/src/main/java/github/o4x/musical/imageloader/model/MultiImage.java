package github.o4x.musical.imageloader.model;

import github.o4x.musical.model.Artist;
import github.o4x.musical.model.Genre;
import github.o4x.musical.model.Playlist;
import github.o4x.musical.model.Song;

import java.util.ArrayList;
import java.util.List;

public class MultiImage {
    public final long id;
    public final String name;
    // filePath to get the image of the artist
    public final List<AlbumCover> albumCovers;

    public MultiImage(long id, String name, final List<AlbumCover> albumCovers) {
        this.id = id;
        this.name = name;
        this.albumCovers = albumCovers;
    }

    static public MultiImage fromArtist(Artist artist) {
        final List<AlbumCover> covers = new ArrayList<>();
//        for (final Album album : artist.albums) {
//            final Song song = album.safeGetFirstSong();
//            covers.add(new AlbumCover(album.getYear(), song.data));
//        }

        return new MultiImage(artist.getId(), artist.getName(), covers);
    }

    static public MultiImage fromGenre(Genre genre) {
        final List<AlbumCover> covers = new ArrayList<>();
        for (final Song song : genre.getSongs()) {
            covers.add(new AlbumCover(song.getAlbumId(), song.getData()));
        }

        return new MultiImage(genre.getId(), genre.getName(), covers);
    }

    static public MultiImage fromPlaylist(Playlist playlist, List<Song> songs) {
        final List<AlbumCover> covers = new ArrayList<>();
        for (final Song song : songs) {
            covers.add(new AlbumCover(song.getAlbumId(), song.getData()));
        }

        return new MultiImage(playlist.getId(), playlist.getName(), covers);
    }

    public String toIdString() {
        StringBuilder id = new StringBuilder();
        id.append(name);
        for (AlbumCover albumCover: albumCovers) {
            id.append(albumCover.getId()).append(albumCover.getFilePath());
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
