package com.o4x.musical.ui.activities.tageditor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.o4x.musical.R;
import com.o4x.musical.loader.SongLoader;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.SongSearchActivity;

import org.jaudiotagger.tag.FieldKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongTagEditorActivity extends AbsTagEditorActivity {

    @BindView(R.id.title1)
    EditText songTitle;
    @BindView(R.id.title2)
    EditText albumTitle;
    @BindView(R.id.artist)
    EditText artist;
    @BindView(R.id.genre)
    EditText genre;
    @BindView(R.id.year)
    EditText year;
    @BindView(R.id.image_text)
    EditText trackNumber;
    @BindView(R.id.lyrics)
    EditText lyrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setNoImageMode();
        setUpViews();

        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.action_tag_editor);
    }

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

    private void setUpViews() {
        fillViewsWithFileTags();
        songTitle.addTextChangedListener(textWatcher);
        albumTitle.addTextChangedListener(textWatcher);
        artist.addTextChangedListener(textWatcher);
        genre.addTextChangedListener(textWatcher);
        year.addTextChangedListener(textWatcher);
        trackNumber.addTextChangedListener(textWatcher);
        lyrics.addTextChangedListener(textWatcher);
    }

    private void fillViewsWithFileTags() {
        songTitle.setText(tagUtil.getSongTitle());
        albumTitle.setText(tagUtil.getAlbumTitle());
        artist.setText(tagUtil.getArtistName());
        genre.setText(tagUtil.getGenreName());
        year.setText(tagUtil.getSongYear());
        trackNumber.setText(tagUtil.getTrackNumber());
        lyrics.setText(tagUtil.getLyrics());
    }

    @Override
    protected void save() {
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        fieldKeyValueMap.put(FieldKey.TITLE, songTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ARTIST, artist.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genre.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());
        fieldKeyValueMap.put(FieldKey.TRACK, trackNumber.getText().toString());
        fieldKeyValueMap.put(FieldKey.LYRICS, lyrics.getText().toString());
        tagUtil.writeValuesToFiles(fieldKeyValueMap, null);
    }

    @Override
    protected void loadCurrentImage() {

    }

    @Override
    protected void getImageFromLastFM() {

    }

    @Override
    protected void searchImageOnWeb() {

    }

    @Override
    protected void deleteImage() {

    }



    @Override
    protected void fillViewsWithResult(Serializable result) {

    }

    @Override
    protected void loadImageFromFile(Uri imageFilePath) {

    }

    @Override
    protected void loadImageFromUrl(String url) {

    }

    @Override
    protected void searchOnline() {
        Intent intent = new Intent(this, SongSearchActivity.class);
        intent.putExtra(SongSearchActivity.EXTRA_SONG_NAME, tagUtil.getSongTitle());
        this.startActivityForResult(intent, SongSearchActivity.REQUEST_CODE);
    }


    @Override
    protected void setColors(int color) {
        super.setColors(color);
        int toolbarTitleColor = ToolbarContentTintHelper.toolbarTitleColor(this, color);
        songTitle.setTextColor(toolbarTitleColor);
        albumTitle.setTextColor(toolbarTitleColor);
    }
}
