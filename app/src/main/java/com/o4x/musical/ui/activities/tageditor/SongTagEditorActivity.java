package com.o4x.musical.ui.activities.tageditor;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.o4x.musical.R;
import com.o4x.musical.loader.SongLoader;
import com.o4x.musical.network.Models.ITunesModel;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.SongSearchActivity;

import java.util.ArrayList;
import java.util.List;

public class SongTagEditorActivity extends AbsTagEditorActivity<ITunesModel.Results> {


    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_song_tag_editor;
    }

    @NonNull
    @Override
    protected List<String> getSongPaths() {
        List<String> paths = new ArrayList<>(1);
        paths.add(SongLoader.getSong(this, getId()).data);
        return paths;
    }

    @Override
    protected void fillViewsWithResult(ITunesModel.Results result) {
        loadImageFromUrl(result.getBigArtworkUrl(), result.collectionName);
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

    @Override
    protected void searchImageOnWeb() {
        searchWebFor(
                songName.getText().toString(),
                albumName.getText().toString(),
                artistName.getText().toString()
        );
    }

    @Override
    protected void searchOnline() {
        Intent intent = new Intent(this, SongSearchActivity.class);
        intent.putExtra(SongSearchActivity.EXTRA_SONG_NAME, tagUtil.getSongTitle());
        this.startActivityForResult(intent, SongSearchActivity.REQUEST_CODE);
    }

    @Override
    protected void getImageFromLastFM() {

    }

}
