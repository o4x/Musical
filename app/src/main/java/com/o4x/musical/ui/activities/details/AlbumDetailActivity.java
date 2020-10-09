package com.o4x.musical.ui.activities.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.interfaces.LoaderIds;
import com.o4x.musical.misc.WrappedAsyncTaskLoader;
import com.o4x.musical.model.Album;
import com.o4x.musical.model.Song;
import com.o4x.musical.repository.RealAlbumRepository;
import com.o4x.musical.repository.RealSongRepository;
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity;
import com.o4x.musical.ui.activities.tageditor.AlbumTagEditorActivity;
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog;
import com.o4x.musical.ui.dialogs.DeleteSongsDialog;
import com.o4x.musical.ui.dialogs.SleepTimerDialog;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.util.PhonographColorUtil;
import com.o4x.musical.util.PreferenceUtil;

import java.util.List;
import java.util.Locale;

/**
 * Be careful when changing things in this Activity!
 */
public class AlbumDetailActivity extends AbsDetailActivity<Album> {

    private static final int LOADER_ID = LoaderIds.ALBUM_DETAIL_ACTIVITY;
    public static final String EXTRA_ALBUM_ID = "extra_album_id";

    private Album album;

    @Override
    void setupViews() {
        super.setupViews();
        headerView.setOnClickListener(v -> {
            if (album != null) {
                NavigationUtil.goToArtist(AlbumDetailActivity.this, album.getArtistId());
            }
        });
    }

    @Override
    void loadImage() {
        getImageLoader().loadImage(album);
    }

    @Override
    void reload() {
        getSupportLoaderManager().restartLoader(LOADER_ID, getIntent().getExtras(), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void loadWiki() {
        loadWiki(Locale.getDefault().getLanguage());
    }

    private void loadWiki(@Nullable final String lang) {
        wiki = null;

//        ApiClient.getClient(this).create(LastFMService.class)
//                .getAlbumInfo(getAlbum().getTitle(), getAlbum().getArtistName(), lang)
//                .enqueue(new Callback<LastFmAlbum>() {
//                    @Override
//                    public void onResponse(@NonNull Call<LastFmAlbum> call, @NonNull Response<LastFmAlbum> response) {
//                        final LastFmAlbum lastFmAlbum = response.body();
//                        if (lastFmAlbum != null && lastFmAlbum.getAlbum() != null && lastFmAlbum.getAlbum().getWiki() != null) {
//                            final String wikiContent = lastFmAlbum.getAlbum().getWiki().getContent();
//                            if (wikiContent != null && !wikiContent.trim().isEmpty()) {
//                                wiki = Html.fromHtml(wikiContent);
//                            }
//                        }
//
//                        // If the "lang" parameter is set and no wiki is given, retry with default language
//                        if (wiki == null && lang != null) {
//                            loadWiki(null);
//                            return;
//                        }
//
//                        if (!PreferenceUtil.isAllowedToDownloadMetadata(AlbumDetailActivity.this)) {
//                            if (wiki != null) {
//                                wikiDialog.setContent(wiki);
//                            } else {
//                                wikiDialog.dismiss();
//                                Toast.makeText(AlbumDetailActivity.this, getResources().getString(R.string.wiki_unavailable), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<LastFmAlbum> call, @NonNull Throwable t) {
//                        t.printStackTrace();
//                    }
//                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final List<Song> songs = songAdapter.getDataSet();
        switch (id) {
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case R.id.action_shuffle_album:
                MusicPlayerRemote.openAndShuffleQueue(songs, true);
                return true;
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(songs);
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(songs);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(songs).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_delete_from_device:
                DeleteSongsDialog.create(songs).show(getSupportFragmentManager(), "DELETE_SONGS");
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(this, AlbumTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, getAlbum().getId());
                startActivityForResult(intent, TAG_EDITOR_REQUEST);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(this, getAlbum().getArtistId());
                return true;
            case R.id.action_wiki:
                if (wikiDialog == null) {
                    wikiDialog = new MaterialDialog.Builder(this)
                            .title(album.getTitle())
                            .positiveText(android.R.string.ok)
                            .build();
                }
                if (PreferenceUtil.isAllowedToDownloadMetadata(this)) {
                    if (wiki != null) {
                        wikiDialog.setContent(wiki);
                        wikiDialog.show();
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.wiki_unavailable), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    wikiDialog.show();
                    loadWiki();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAlbum(Album album) {
        this.album = album;
        loadImage();

        if (PreferenceUtil.isAllowedToDownloadMetadata(this)) {
            loadWiki();
        }

//        artistTextView.setText(album.getArtistName());
//        songCountTextView.setText(MusicUtil.getSongCountString(this, album.getSongCount()));
//        durationTextView.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(this, album.getSongs())));
//        albumYearTextView.setText(MusicUtil.getYearString(album.getYear()));
        getSupportActionBar().setTitle(album.getTitle());
        title.setText(album.getTitle());
        subtitle.setText(album.getArtistName());

        songAdapter.swapDataSet(album.getSongs());
    }

    private Album getAlbum() {
        if (album == null) album = Album.Companion.getEmpty();
        return album;
    }

    @Override
    protected List<Song> getSongs() {
        return getAlbum().getSongs();
    }

    @Override
    public Loader<Album> onCreateLoader(int id, Bundle args) {
        return new AsyncAlbumLoader(this, args.getLong(EXTRA_ALBUM_ID));
    }

    @Override
    public void onLoadFinished(Loader<Album> loader, Album data) {
        setAlbum(data);
    }

    @Override
    public void onLoaderReset(Loader<Album> loader) {
        this.album = Album.Companion.getEmpty();
        songAdapter.swapDataSet(album.getSongs());
    }

    private static class AsyncAlbumLoader extends WrappedAsyncTaskLoader<Album> {
        private final long albumId;

        public AsyncAlbumLoader(Context context, long albumId) {
            super(context);
            this.albumId = albumId;
        }

        @Override
        public Album loadInBackground() {
            return new RealAlbumRepository(new RealSongRepository(getContext())).album(albumId);
        }
    }
}
