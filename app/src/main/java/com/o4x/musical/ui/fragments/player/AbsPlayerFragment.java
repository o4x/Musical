package com.o4x.musical.ui.fragments.player;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.interfaces.PaletteColorHolder;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity;
import com.o4x.musical.ui.activities.tageditor.SongTagEditorActivity;
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog;
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog;
import com.o4x.musical.ui.dialogs.SleepTimerDialog;
import com.o4x.musical.ui.dialogs.SongDetailDialog;
import com.o4x.musical.ui.dialogs.SongShareDialog;
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.NavigationUtil;

public abstract class AbsPlayerFragment extends AbsMusicServiceFragment implements Toolbar.OnMenuItemClickListener, PaletteColorHolder {

    private Callbacks callbacks;
    private static boolean isToolbarShown = true;

//    protected Unbinder unbinder;
//
//    @Nullable
//    @BindView(R.id.toolbar_container)
//    protected FrameLayout toolbarContainer;
//    @BindView(R.id.player_toolbar)
//    protected Toolbar toolbar;
//    @BindView(R.id.player_sliding_layout)
//    protected SlidingUpPanelLayout slidingUpPanelLayout;
//    @BindView(R.id.player_recycler_view)
//    protected RecyclerView recyclerView;
//    @BindView(R.id.playing_queue_card)
//    protected CardView playingQueueCard;
//    @BindView(R.id.color_background)
//    protected View colorBackground;
//    @BindView(R.id.player_queue_sub_header)
//    protected TextView playerQueueSubHeader;
//
//    protected int lastColor;
//
//    protected CardPlayerPlaybackControlsFragment playbackControlsFragment;
//    protected PlayerAlbumCoverFragment playerAlbumCoverFragment;
//
//    protected LinearLayoutManager layoutManager;
//
//    protected PlayingQueueAdapter playingQueueAdapter;
//
//    protected RecyclerView.Adapter wrappedAdapter;
//    protected RecyclerViewDragDropManager recyclerViewDragDropManager;
//
//    protected AsyncTask updateIsFavoriteTask;
//    protected AsyncTask updateLyricsAsyncTask;
//
//    protected Lyrics lyrics;
//
//    protected Impl impl;

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
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_toggle_favorite:
                toggleFavorite(song);
                return true;
            case R.id.action_share:
                SongShareDialog.create(song).show(getFragmentManager(), "SHARE_SONG");
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(getActivity());
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(song).show(getFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_clear_playing_queue:
                MusicPlayerRemote.clearQueue();
                return true;
            case R.id.action_save_playing_queue:
                CreatePlaylistDialog.create(MusicPlayerRemote.getPlayingQueue()).show(getActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(getActivity(), SongTagEditorActivity.class);
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id);
                startActivity(intent);
                return true;
            case R.id.action_details:
                SongDetailDialog.create(song).show(getFragmentManager(), "SONG_DETAIL");
                return true;
            case R.id.action_go_to_album:
                NavigationUtil.goToAlbum(getActivity(), song.albumId);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(getActivity(), song.artistId);
                return true;
        }
        return false;
    }

    protected void toggleFavorite(Song song) {
        MusicUtil.toggleFavorite(getActivity(), song);
    }

    protected boolean isToolbarShown() {
        return isToolbarShown;
    }

    protected void setToolbarShown(boolean toolbarShown) {
        isToolbarShown = toolbarShown;
    }

    protected void showToolbar(@Nullable final View toolbar) {
        if (toolbar == null) return;

        setToolbarShown(true);

        toolbar.setVisibility(View.VISIBLE);
        toolbar.animate().alpha(1f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION);
    }

    protected void hideToolbar(@Nullable final View toolbar) {
        if (toolbar == null) return;

        setToolbarShown(false);

        toolbar.animate().alpha(0f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION).withEndAction(() -> toolbar.setVisibility(View.GONE));
    }

    protected void toggleToolbar(@Nullable final View toolbar) {
        if (isToolbarShown()) {
            hideToolbar(toolbar);
        } else {
            showToolbar(toolbar);
        }
    }

    protected void checkToggleToolbar(@Nullable final View toolbar) {
        if (toolbar != null && !isToolbarShown() && toolbar.getVisibility() != View.GONE) {
            hideToolbar(toolbar);
        } else if (toolbar != null && isToolbarShown() && toolbar.getVisibility() != View.VISIBLE) {
            showToolbar(toolbar);
        }
    }

    protected String getUpNextAndQueueTime() {
        final long duration = MusicPlayerRemote.getQueueDurationMillis(MusicPlayerRemote.getPosition());

        return MusicUtil.buildInfoString(
            getResources().getString(R.string.up_next),
            MusicUtil.getReadableDurationString(duration)
        );
    }

    public abstract void onShow();

    public abstract void onHide();

    public abstract boolean onBackPressed();

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

        void setUpPanelAndAlbumCoverHeight();
    }
}
