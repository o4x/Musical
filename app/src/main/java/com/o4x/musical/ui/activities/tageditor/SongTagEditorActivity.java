package com.o4x.musical.ui.activities.tageditor;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.o4x.musical.R;
import com.o4x.musical.loader.SongLoader;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.SongSearchActivity;

import java.util.ArrayList;
import java.util.List;

public class SongTagEditorActivity extends AbsTagEditorActivity {


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
    protected void setColors(int color) {
        super.setColors(color);
        int toolbarTitleColor = ToolbarContentTintHelper.toolbarTitleColor(this, color);
        songName.setTextColor(toolbarTitleColor);
        albumName.setTextColor(toolbarTitleColor);
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
