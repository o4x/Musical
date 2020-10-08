package com.o4x.musical.ui.activities.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.interfaces.LoaderIds;
import com.o4x.musical.misc.WrappedAsyncTaskLoader;
import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Song;
import com.o4x.musical.repository.RealAlbumRepository;
import com.o4x.musical.repository.RealArtistRepository;
import com.o4x.musical.repository.RealSongRepository;
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity;
import com.o4x.musical.ui.activities.tageditor.ArtistTagEditorActivity;
import com.o4x.musical.ui.adapter.album.HorizontalAlbumAdapter;
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog;
import com.o4x.musical.ui.dialogs.SleepTimerDialog;
import com.o4x.musical.util.CustomImageUtil;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.util.PreferenceUtil;

import java.util.List;
import java.util.Locale;

/**
 * Be careful when changing things in this Activity!
 */
public class ArtistDetailActivity extends AbsDetailActivity<Artist> {

    private static final int LOADER_ID = LoaderIds.ARTIST_DETAIL_ACTIVITY;
    public static final String EXTRA_ARTIST_ID = "extra_artist_id";


    View songListHeader;
    RecyclerView albumRecyclerView;
    private Artist artist;
    private HorizontalAlbumAdapter albumAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
//        songListHeader = LayoutInflater.from(this).inflate(R.layout.artist_detail_header, recyclerView, false);
//        albumRecyclerView = songListHeader.findViewById(R.id.recycler_view);
//        setupAlbumRecyclerView();
//        recyclerView.addHeaderView(songListHeader);
    }

    private void setupAlbumRecyclerView() {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        albumAdapter = new HorizontalAlbumAdapter(this, getArtist().getAlbums(), true, this);
        albumRecyclerView.setAdapter(albumAdapter);
        albumAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (albumAdapter.getItemCount() == 0) finish();
            }
        });
    }

    void reload() {
        getSupportLoaderManager().restartLoader(LOADER_ID, getIntent().getExtras(), this);
    }

    private void loadBiography() {
        loadBiography(Locale.getDefault().getLanguage());
    }

    private void loadBiography(@Nullable final String lang) {
        wiki = null;

//        ApiClient.getClient(this).create(LastFMService.class)
//                .getArtistInfo(getArtist().getName(), lang, null)
//                .enqueue(new Callback<LastFmArtist>() {
//                    @Override
//                    public void onResponse(@NonNull Call<LastFmArtist> call, @NonNull Response<LastFmArtist> response) {
//                        final LastFmArtist lastFmArtist = response.body();
//                        if (lastFmArtist != null && lastFmArtist.getArtist() != null) {
//                            final String bioContent = lastFmArtist.getArtist().getBio().getContent();
//                            if (bioContent != null && !bioContent.trim().isEmpty()) {
//                                biography = Html.fromHtml(bioContent);
//                            }
//                        }
//
//                        // If the "lang" parameter is set and no biography is given, retry with default language
//                        if (biography == null && lang != null) {
//                            loadBiography(null);
//                            return;
//                        }
//
//                        if (!PreferenceUtil.isAllowedToDownloadMetadata(ArtistDetailActivity.this)) {
//                            if (biography != null) {
//                                biographyDialog.setContent(biography);
//                            } else {
//                                biographyDialog.dismiss();
//                                Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.biography_unavailable), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<LastFmArtist> call, @NonNull Throwable t) {
//                        t.printStackTrace();
//                        biography = null;
//                    }
//                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        return super.onCreateOptionsMenu(menu);
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
            case R.id.action_shuffle_artist:
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
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_biography:
                if (wikiDialog == null) {
                    wikiDialog = new MaterialDialog.Builder(this)
                            .title(artist.getName())
                            .positiveText(android.R.string.ok)
                            .build();
                }
                if (PreferenceUtil.isAllowedToDownloadMetadata(ArtistDetailActivity.this)) { // wiki should've been already downloaded
                    if (wiki != null) {
                        wikiDialog.setContent(wiki);
                        wikiDialog.show();
                    } else {
                        Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.biography_unavailable), Toast.LENGTH_SHORT).show();
                    }
                } else { // force download
                    wikiDialog.show();
                    loadBiography();
                }
                return true;
            case R.id.action_reset_artist_image:
                Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.updating), Toast.LENGTH_SHORT).show();
                new CustomImageUtil(artist).resetCustomImage();
                return true;
            case R.id.action_tag_editor:
                Intent editor = new Intent(this, ArtistTagEditorActivity.class);
                editor.putExtra(AbsTagEditorActivity.EXTRA_ID, getArtist().getId());
                startActivityForResult(editor, TAG_EDITOR_REQUEST);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setArtist(Artist artist) {
        this.artist = artist;
        loadImage();

        if (PreferenceUtil.isAllowedToDownloadMetadata(this)) {
            loadBiography();
        }


//        songCountTextView.setText(MusicUtil.getSongCountString(this, artist.getSongCount()));
//        albumCountTextView.setText(MusicUtil.getAlbumCountString(this, artist.getAlbumCount()));
//        durationTextView.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(this, artist.getSongs())));

        songAdapter.swapDataSet(artist.getSongs());
//        albumAdapter.swapDataSet(artist.getAlbums());
    }

    @Override
    protected List<Song> getSongs() {
        return getArtist().getSongs();
    }

    private Artist getArtist() {
        if (artist == null) artist = Artist.Companion.getEmpty();
        return artist;
    }

    @Override
    public Loader<Artist> onCreateLoader(int id, Bundle args) {
        return new AsyncArtistDataLoader(this, args.getLong(EXTRA_ARTIST_ID));
    }

    @Override
    public void onLoadFinished(Loader<Artist> loader, Artist data) {
        setArtist(data);
    }

    @Override
    public void onLoaderReset(Loader<Artist> loader) {
        this.artist = Artist.Companion.getEmpty();
        songAdapter.swapDataSet(artist.getSongs());
        albumAdapter.swapDataSet(artist.getAlbums());
    }

    private static class AsyncArtistDataLoader extends WrappedAsyncTaskLoader<Artist> {
        private final long artistId;

        public AsyncArtistDataLoader(Context context, long artistId) {
            super(context);
            this.artistId = artistId;
        }

        @Override
        public Artist loadInBackground() {
            return new RealArtistRepository(
                    new RealSongRepository(getContext()),
                    new RealAlbumRepository(new RealSongRepository(getContext()))
            ).artist(artistId);
        }
    }
}
