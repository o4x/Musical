package com.o4x.musical.ui.fragments.mainactivity.queue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.o4x.musical.R;
import com.o4x.musical.dialogs.CreatePlaylistDialog;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.interfaces.MusicServiceEventListener;
import com.o4x.musical.loader.SongLoader;
import com.o4x.musical.ui.activities.MainActivity;
import com.o4x.musical.ui.activities.SearchActivity;
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity;
import com.o4x.musical.ui.adapter.song.PlayingQueueAdapter;
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class QueueFragment extends AbsMainActivityFragment implements MainActivity.MainActivityFragmentCallbacks {
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

    private PlayingQueueAdapter queueAdapter;
    private LinearLayoutManager queueLayoutManager;
    private QueueFragment.QueueListener queueListener;

    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;

    public static QueueFragment newInstance() { return new QueueFragment(); }

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
        View view = inflater.inflate(R.layout.fragment_queue, container, false);
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
        queueListener = new QueueFragment.QueueListener();
        activity.addMusicServiceEventListener(queueListener);

        getMainActivity().setStatusBarColorAuto();
        getMainActivity().setNavigationBarColorAuto();
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
        checkIsEmpty();
    }

    private void setUpQueueView() {

        recyclerViewDragDropManager = new RecyclerViewDragDropManager();
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        queueAdapter = new PlayingQueueAdapter(
                ((AppCompatActivity) getActivity()),
                MusicPlayerRemote.getPlayingQueue(),
                MusicPlayerRemote.getPosition(),
                R.layout.item_list,
                false,
                null);
        wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(queueAdapter);

        queueLayoutManager = new LinearLayoutManager(getActivity());

        queueView.setLayoutManager(queueLayoutManager);
        queueView.setAdapter(wrappedAdapter);
        queueView.setItemAnimator(animator);

        recyclerViewDragDropManager.attachRecyclerView(queueView);

        queueLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition() + 1, 0);



//        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
//        queueLayoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
//        queueView.setLayoutManager(queueLayoutManager);
//        queueAdapter = new PlayingQueueAdapter(
//                ((AppCompatActivity) getActivity()),
//                MusicPlayerRemote.getPlayingQueue(),
//                MusicPlayerRemote.getPosition(),
//                R.layout.item_list_no_image,
//                false,
//                null
//        );
//        queueView.setAdapter(queueAdapter);
//        queueView.setItemAnimator(animator);
//        queueLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition(), 0);
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
            checkIsEmpty();
        }

        private void resetToCurrentPosition() {
            if (queueAdapter.getItemCount() == 0) return;
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

    private void checkIsEmpty() {
        if (empty != null) {
            empty.setVisibility(
                    (queueAdapter == null || queueAdapter.getItemCount() == 0)
                            ? View.VISIBLE : View.GONE
            );
        }
    }
}
