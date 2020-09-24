package com.o4x.musical.ui.activities.tageditor;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.o4x.musical.R;
import com.o4x.musical.loader.ArtistLoader;
import com.o4x.musical.model.Song;
import com.o4x.musical.network.Models.DeezerArtistModel;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.ArtistSearchActivity;

import java.util.ArrayList;
import java.util.List;

public class ArtistTagEditorActivity extends AbsTagEditorActivity<DeezerArtistModel.Data> {

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_artist_tag_editor;
    }

    @NonNull
    @Override
    protected List<String> getSongPaths() {
        List<Song> songs = ArtistLoader.getArtist(this, getId()).getSongs();
        List<String> paths = new ArrayList<>(songs.size());
        for (Song song : songs) {
            paths.add(song.data);
        }
        return paths;
    }

    @Override
    protected void fillViewsWithResult(DeezerArtistModel.Data result) {
        loadImageFromUrl(result.pictureXl, result.name);
        if (artistName != null)
            artistName.setText(result.name);
    }

    @Override
    protected void searchImageOnWeb() {
        searchWebFor(artistName.getText().toString());
    }

    @Override
    protected void searchOnline() {
        Intent intent = new Intent(this, ArtistSearchActivity.class);
        intent.putExtra(ArtistSearchActivity.EXTRA_SONG_NAME, tagUtil.getArtistName());
        this.startActivityForResult(intent, ArtistSearchActivity.REQUEST_CODE);
    }

    @Override
    protected void getImageFromLastFM() {
        
    }

}
