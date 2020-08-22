package com.o4x.musical.ui.activities.tageditor;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.widget.Toast;

import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.o4x.musical.R;
import com.o4x.musical.network.ApiClient;
import com.o4x.musical.network.Models.ITunesModel;
import com.o4x.musical.network.Models.LastFmAlbum;
import com.o4x.musical.loader.AlbumLoader;
import com.o4x.musical.model.Song;
import com.o4x.musical.network.service.LastFMService;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity;
import com.o4x.musical.util.LastFMUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumTagEditorActivity extends AbsTagEditorActivity<ITunesModel.Results> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @Override
    protected void fillViewsWithResult(ITunesModel.Results result) {
        loadImageFromUrl(result.getBigArtworkUrl());
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
    protected int getContentViewLayout() {
        return R.layout.activity_album_tag_editor;
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
    protected void setColors(int color) {
        super.setColors(color);
        albumName.setTextColor(ToolbarContentTintHelper.toolbarTitleColor(this, color));
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

    @Override
    protected void getImageFromLastFM() {
        String albumTitleStr = albumName.getText().toString();
        String albumArtistNameStr = artistName.getText().toString();
        if (albumArtistNameStr.trim().equals("") || albumTitleStr.trim().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.album_or_artist_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.getClient(this).create(LastFMService.class)
                .getAlbumInfo(albumTitleStr, albumArtistNameStr, null).enqueue(new Callback<LastFmAlbum>() {
            @Override
            public void onResponse(Call<LastFmAlbum> call, Response<LastFmAlbum> response) {
                LastFmAlbum lastFmAlbum = response.body();
                if (lastFmAlbum.getAlbum() != null) {
                    String url = LastFMUtil.getLargestAlbumImageUrl(lastFmAlbum.getAlbum().getImage());
                    if (!TextUtils.isEmpty(url) && url.trim().length() > 0) {
                        loadImageFromUrl(url);
                        return;
                    }
                }
                toastLoadingFailed();
            }

            @Override
            public void onFailure(Call<LastFmAlbum> call, Throwable t) {
                toastLoadingFailed();
            }

            private void toastLoadingFailed() {
                Toast.makeText(AlbumTagEditorActivity.this,
                        R.string.could_not_download_album_cover, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
