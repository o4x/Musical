package com.o4x.musical.ui.fragments.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.interfaces.PaletteColorHolder;
import com.o4x.musical.model.Song;
import com.o4x.musical.model.lyrics.Lyrics;
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity;
import com.o4x.musical.ui.activities.tageditor.SongTagEditorActivity;
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog;
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog;
import com.o4x.musical.ui.dialogs.LyricsDialog;
import com.o4x.musical.ui.dialogs.SleepTimerDialog;
import com.o4x.musical.ui.dialogs.SongDetailDialog;
import com.o4x.musical.ui.dialogs.SongShareDialog;
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment;
import com.o4x.musical.util.ImageUtil;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.util.Util;
import com.o4x.musical.util.color.MediaNotificationProcessor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;

public abstract class AbsPlayerFragment extends AbsMusicServiceFragment
        implements Toolbar.OnMenuItemClickListener,
        PaletteColorHolder {

    public AbsPlayerFragment(int layout) {
        super(layout);
    }

    private Callbacks callbacks;

    protected Unbinder unbinder;

    @BindView(R.id.player_toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.content)
    protected LinearLayout content;

    protected int lastColor;

    protected PlayerPlaybackControlsFragments playbackControlsFragment;
    protected PlayerAlbumCoverFragment playerAlbumCoverFragment;

    protected AsyncTask updateLyricsAsyncTask;

    protected Lyrics lyrics;

    protected Impl impl;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        impl.init();

        setUpPlayerToolbar();
        setUpSubFragments();

        if (!Util.isLandscape(getResources())) {
            content.setOnApplyWindowInsetsListener(
                    (view1, windowInsets) -> {
                        content.onApplyWindowInsets(windowInsets);
                        getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment).getView().setPadding(
                                windowInsets.getSystemWindowInsetLeft(), 0, windowInsets.getSystemWindowInsetRight(), windowInsets.getSystemWindowInsetBottom()
                        );
                        return windowInsets;
                    }
            );
        }

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        serviceActivity.getPlayerViewModel().getCurrentPalette().observe(getViewLifecycleOwner(),
                colors -> {
                    animateColorChange(ColorUtil.INSTANCE.withAlpha(colors.getBackgroundColor(), 0.7f));
                    playbackControlsFragment.setColor(colors);
                    getCallbacks().onPaletteColorChanged();
                }
        );
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must implement " + Callbacks.class.getSimpleName());
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final Song song = MusicPlayerRemote.getCurrentSong();
        switch (item.getItemId()) {
            case R.id.action_show_lyrics:
                if (lyrics != null)
                    LyricsDialog.create(lyrics).show(getFragmentManager(), "LYRICS");
                return true;
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_share:
                SongShareDialog.create(song).show(getFragmentManager(), "SHARE_SONG");
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(getServiceActivity());
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(song).show(getFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_clear_playing_queue:
                MusicPlayerRemote.clearQueue();
                return true;
            case R.id.action_save_playing_queue:
                CreatePlaylistDialog.create(MusicPlayerRemote.getPlayingQueue()).show(getServiceActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(getServiceActivity(), SongTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.getId());
                startActivity(intent);
                return true;
            case R.id.action_details:
                SongDetailDialog.create(song).show(getFragmentManager(), "SONG_DETAIL");
                return true;
            case R.id.action_go_to_album:
                NavigationUtil.goToAlbum(getServiceActivity(), song.getAlbumId());
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(getServiceActivity(), song.getArtistId());
                return true;
        }
        return false;
    }

    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onServiceConnected() {
        updateCurrentSong();
        updateLyrics();
    }

    @Override
    public void onPlayingMetaChanged() {
        updateCurrentSong();
        updateLyrics();
    }

    @Override
    public void onQueueChanged() { }

    @Override
    public void onMediaStoreChanged() { }

    @SuppressWarnings("ConstantConditions")
    private void updateCurrentSong() {
        impl.updateCurrentSong(MusicPlayerRemote.getCurrentSong());
    }

    private void setUpSubFragments() {
        playbackControlsFragment = (PlayerPlaybackControlsFragments) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
    }

    private void setUpPlayerToolbar() {
        toolbar.inflateMenu(R.menu.menu_player);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> getServiceActivity().onBackPressed());
        toolbar.setOnMenuItemClickListener(this);
    }

    @SuppressLint("StaticFieldLeak")
    private void updateLyrics() {
        if (updateLyricsAsyncTask != null) updateLyricsAsyncTask.cancel(false);
        final Song song = MusicPlayerRemote.getCurrentSong();
        updateLyricsAsyncTask = new AsyncTask<Void, Void, Lyrics>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                lyrics = null;
                playerAlbumCoverFragment.setLyrics(null);
                toolbar.getMenu().removeItem(R.id.action_show_lyrics);
            }

            @Override
            protected Lyrics doInBackground(Void... params) {
                String data = MusicUtil.getLyrics(song);
                if (TextUtils.isEmpty(data)) {
                    return null;
                }
                return Lyrics.parse(song, data);
            }

            @Override
            protected void onPostExecute(Lyrics l) {
                lyrics = l;
                playerAlbumCoverFragment.setLyrics(lyrics);
                if (lyrics == null) {
                    if (toolbar != null) {
                        toolbar.getMenu().removeItem(R.id.action_show_lyrics);
                    }
                } else {
                    Activity activity = getServiceActivity();
                    if (toolbar != null && activity != null)
                        if (toolbar.getMenu().findItem(R.id.action_show_lyrics) == null) {
                            int color = ToolbarContentTintHelper.toolbarContentColor(activity, Color.TRANSPARENT);
                            Drawable drawable = ImageUtil.getTintedVectorDrawable(activity, R.drawable.ic_comment_text_outline, color);
                            toolbar.getMenu()
                                    .add(Menu.NONE, R.id.action_show_lyrics, Menu.NONE, R.string.action_show_lyrics)
                                    .setIcon(drawable)
                                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                        }
                }
            }

            @Override
            protected void onCancelled(Lyrics s) {
                onPostExecute(null);
            }
        }.execute();
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastColor;
    }


    private void animateColorChange(final int newColor) {
        impl.animateColorChange(newColor);
        lastColor = newColor;
    }

    public Callbacks getCallbacks() {
        return callbacks;
    }

    public interface Callbacks {
        void onPaletteColorChanged();
    }

    protected interface Impl {
        void init();

        void updateCurrentSong(Song song);

        void animateColorChange(final int newColor);
    }
}
