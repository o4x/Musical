package com.o4x.musical.ui.activities.tageditor;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.o4x.musical.R;
import com.o4x.musical.lastfm.rest.model.LastFmAlbum;
import com.o4x.musical.loader.AlbumLoader;
import com.o4x.musical.loader.ArtistLoader;
import com.o4x.musical.model.Album;
import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.ArtistSearchActivity;
import com.o4x.musical.util.LastFMUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistTagEditorActivity extends AbsTagEditorActivity {

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
    protected void setColors(int color) {
        super.setColors(color);
        artistName.setTextColor(ToolbarContentTintHelper.toolbarTitleColor(this, color));
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
