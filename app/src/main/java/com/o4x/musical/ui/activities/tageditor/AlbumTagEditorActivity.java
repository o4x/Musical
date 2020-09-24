package com.o4x.musical.ui.activities.tageditor;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.o4x.musical.R;
import com.o4x.musical.loader.AlbumLoader;
import com.o4x.musical.loader.ArtistLoader;
import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Song;
import com.o4x.musical.network.Models.ITunesModel;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class AlbumTagEditorActivity extends AbsTagEditorActivity<ITunesModel.Results> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_album_tag_editor;
    }


    @Override
    protected void fillViewsWithResult(ITunesModel.Results result) {
        loadImageFromUrl(result.getBigArtworkUrl(), null);
        if (songName != null)
            songName.setText(result.trackName);
        if (albumName != null)
            albumName.setText(result.collectionName);
        if (artistName != null)
            artistName.setText(result.artistName);
        if (genreName != null)
            genreName.setText(result.primaryGenreName);
        if (year != null)
            year.setText(result.getYear());
        if (trackNumber != null)
            trackNumber.setText(String.valueOf(result.trackNumber));
    }

    @NotNull
    @Override
    protected Artist getArtist() {
        return ArtistLoader.getArtist(
                this,
                AlbumLoader.getAlbum(this, getId()).getArtistId()
        );
    }

    @NonNull
    @Override
    protected List<String> getSongPaths() {
        List<Song> songs = AlbumLoader.getAlbum(this, getId()).songs;
        List<String> paths = new ArrayList<>(songs.size());
        for (Song song : songs) {
            paths.add(song.data);
        }
        return paths;
    }

    @Override
    protected void searchImageOnWeb() {
        searchWebFor(albumName.getText().toString(), artistName.getText().toString());
    }

    @Override
    protected void searchOnline() {
        Intent intent = new Intent(this, AlbumSearchActivity.class);
        intent.putExtra(AlbumSearchActivity.EXTRA_SONG_NAME, tagUtil.getAlbumTitle());
        this.startActivityForResult(intent, AlbumSearchActivity.REQUEST_CODE);
    }
}
