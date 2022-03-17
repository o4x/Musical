package github.o4x.musical.helper;

import android.content.Context;

import github.o4x.musical.model.AbsCustomPlaylist;
import github.o4x.musical.model.Playlist;
import github.o4x.musical.model.Song;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class M3UWriter implements M3UConstants {

    public static File write(Context context, File dir, Playlist playlist) throws IOException {
        if (!dir.exists()) //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        File file = new File(dir, playlist.getName().concat("." + EXTENSION));

        List<Song> songs = playlist.songs();

        if (songs.size() > 0) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            bw.write(HEADER);
            for (Song song : songs) {
                bw.newLine();
                bw.write(ENTRY + song.getDuration() + DURATION_SEPARATOR + song.getArtistName() + " - " + song.getTitle());
                bw.newLine();
                bw.write(song.getData());
            }

            bw.close();
        }

        return file;
    }
}
