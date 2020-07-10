package com.o4x.musical.ui.fragments.mainactivity.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.google.android.material.appbar.AppBarLayout;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.o4x.musical.R;
import com.o4x.musical.dialogs.CreatePlaylistDialog;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.interfaces.MusicServiceEventListener;
import com.o4x.musical.loader.LastAddedLoader;
import com.o4x.musical.loader.SongLoader;
import com.o4x.musical.loader.TopAndRecentlyPlayedTracksLoader;
import com.o4x.musical.misc.SimpleObservableScrollViewCallbacks;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.activities.MainActivity;
import com.o4x.musical.ui.activities.SearchActivity;
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity;
import com.o4x.musical.ui.adapter.album.HorizontalAlbumAdapter;
import com.o4x.musical.ui.adapter.home.HomeAdapter;
import com.o4x.musical.ui.adapter.song.ArtistSongAdapter;
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment;
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends AbsMainActivityFragment implements MainActivity.MainActivityFragmentCallbacks {

    private AbsMusicServiceActivity activity;

    private Unbinder unbinder;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(android.R.id.empty)
    View empty;

    @BindView(R.id.queue_recycler_view)
    RecyclerView queueView;
    @BindView(R.id.recently_recycler_view)
    RecyclerView recentlyView;
    @BindView(R.id.new_recycler_view)
    RecyclerView newView;

    private HomeAdapter queueAdapter;
    private LinearLayoutManager queueLayoutManager;
    private QueueListener queueListener;

    private HomeAdapter recentlyAdapter, newAdapter;
    private GridLayoutManager recentlyLayoutManager, newLayoutManager;

    public static HomeFragment newInstance() { return new HomeFragment(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            activity = (AbsMusicServiceActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + AbsMusicServiceActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        activity.removeMusicServiceEventListener(queueListener);

        queueAdapter = null;
        queueLayoutManager = null;
        queueListener = null;

        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queueListener = new QueueListener();
        activity.addMusicServiceEventListener(queueListener);

        getMainActivity().setStatusbarColorAuto();
        getMainActivity().setNavigationbarColorAuto();
        getMainActivity().setTaskDescriptionColorAuto();

        setUpToolbar();

        setUpViews();
    }

    private void setUpToolbar() {
        int primaryColor = ThemeStore.primaryColor(getActivity());
        appbar.setBackgroundColor(primaryColor);
        toolbar.setBackgroundColor(primaryColor);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        getActivity().setTitle(R.string.app_name);
        getMainActivity().setSupportActionBar(toolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        Activity activity = getActivity();
        if (activity == null) return;
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(getActivity(), toolbar, menu, ATHToolbarActivity.getToolbarBackgroundColor(toolbar));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Activity activity = getActivity();
        if (activity == null) return;
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(activity, toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(getActivity()), true);
                return true;
            case R.id.action_new_playlist:
                CreatePlaylistDialog.create().show(getChildFragmentManager(), "CREATE_PLAYLIST");
                return true;
            case R.id.action_search:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleBackPress() {
        return false;
    }

    private void setUpViews() {
        setUpQueueView();
        setUpRecentlyView();
        setUpNewView();
    }

    private void setUpQueueView() {
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        queueLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        queueView.setLayoutManager(queueLayoutManager);
        queueAdapter = new HomeAdapter(
                ((AppCompatActivity) getActivity()),
                MusicPlayerRemote.getPlayingQueue(),
                MusicPlayerRemote.getPosition(),
                R.layout.item_card_home,
                null,
                false
        );
        queueView.setAdapter(queueAdapter);
        queueView.setItemAnimator(animator);
        queueLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition(), 0);
    }

    private void setUpRecentlyView() {
        recentlyLayoutManager = new GridLayoutManager(getActivity(), 3);
        recentlyView.setLayoutManager(recentlyLayoutManager);
        recentlyAdapter = new HomeAdapter(
                ((AppCompatActivity) getActivity()),
                TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(getContext()),
                0,
                R.layout.item_card_home,
                6,
                false
        );
        recentlyView.setAdapter(recentlyAdapter);
    }

    private void setUpNewView() {
        newLayoutManager = new GridLayoutManager(getActivity(), 3);
        newView.setLayoutManager(newLayoutManager);
        newAdapter = new HomeAdapter(
                ((AppCompatActivity) getActivity()),
                LastAddedLoader.getLastAddedSongs(getContext()),
                0,
                R.layout.item_card_home,
                9,
                false
        );
        newView.setAdapter(newAdapter);
    }

    class QueueListener implements MusicServiceEventListener {
        @Override
        public void onServiceConnected() {
            updateQueue();
        }

        @Override
        public void onServiceDisconnected() {

        }

        @Override
        public void onQueueChanged() {
            updateQueue();
        }

        @Override
        public void onPlayingMetaChanged() {
            resetToCurrentPosition();
        }

        @Override
        public void onPlayStateChanged() {

        }

        @Override
        public void onRepeatModeChanged() {

        }

        @Override
        public void onShuffleModeChanged() {

        }

        @Override
        public void onMediaStoreChanged() {
            updateQueue();
        }

        private void updateQueue() {
            queueAdapter.swapDataSet(MusicPlayerRemote.getPlayingQueue(), MusicPlayerRemote.getPosition());
            resetToCurrentPosition();
        }

        private void resetToCurrentPosition() {
            queueView.stopScroll();
            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
                @Override
                protected int getHorizontalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(MusicPlayerRemote.getPosition());
            queueLayoutManager.startSmoothScroll(smoothScroller);
        }
    }


}
