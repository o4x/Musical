package com.o4x.musical.ui.fragments.mainactivity.home;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.o4x.musical.R;
import com.o4x.musical.dialogs.CreatePlaylistDialog;
import com.o4x.musical.imageloader.glide.SongGlideRequest;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.interfaces.MusicServiceEventListener;
import com.o4x.musical.loader.LastAddedLoader;
import com.o4x.musical.loader.SongLoader;
import com.o4x.musical.loader.TopAndRecentlyPlayedTracksLoader;
import com.o4x.musical.model.Song;
import com.o4x.musical.model.smartplaylist.HistoryPlaylist;
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist;
import com.o4x.musical.ui.activities.MainActivity;
import com.o4x.musical.ui.activities.SearchActivity;
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity;
import com.o4x.musical.ui.adapter.home.HomeAdapter;
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.util.PhonographColorUtil;
import com.o4x.musical.util.Util;
import com.o4x.musical.util.ViewUtil;
import com.xw.repo.widget.BounceScrollView;

import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends AbsMainActivityFragment implements MainActivity.MainActivityFragmentCallbacks {

    private MainActivity activity;

    private Unbinder unbinder;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(android.R.id.empty)
    View empty;

    @BindView(R.id.header)
    View header;
    @BindView(R.id.poster_parent)
    FrameLayout posterParent;
    @BindView(R.id.poster)
    ImageView poster;
    @BindView(R.id.poster_gradient)
    View posterGradient;

    @BindView(R.id.nested_scroll_view)
    BounceScrollView bounceScrollView;
    @BindView(R.id.queue_recycler_view)
    RecyclerView queueView;
    @BindView(R.id.recently_recycler_view)
    RecyclerView recentlyView;
    @BindView(R.id.new_recycler_view)
    RecyclerView newView;

    @BindView(R.id.shuffle_btn)
    FloatingActionButton shuffleBtn;

    @BindView(R.id.queue_parent)
    ConstraintLayout queueParent;
    @BindView(R.id.open_queue_button)
    Button openQueueButton;
    @BindView(R.id.recently_parent)
    RelativeLayout recentlyParent;
    @BindView(R.id.newly_parent)
    RelativeLayout newlyParent;

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
            activity = getMainActivity();
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

        activity.setStatusBarColor(transparentColor());
        activity.setNavigationBarColorAuto();
        activity.setTaskDescriptionColorAuto();

        setUpToolbar();

        setUpViews();
    }

    private int transparentColor() {
        return ColorUtil.withAlpha(ThemeStore.primaryColor(activity), 0);
    }

    private void setUpToolbar() {
        final int transparentColor = transparentColor();
        appbar.setBackgroundColor(transparentColor);
        toolbar.setBackgroundColor(transparentColor);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        activity.setTitle(R.string.app_name);
        activity.setSupportActionBar(toolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        if (activity == null) return;
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(activity, toolbar, menu, ThemeStore.primaryColor(activity));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (activity == null) return;
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(activity, toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(activity), true);
                return true;
            case R.id.action_new_playlist:
                CreatePlaylistDialog.create().show(getChildFragmentManager(), "CREATE_PLAYLIST");
                return true;
            case R.id.action_search:
                startActivity(new Intent(activity, SearchActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleBackPress() {
        return false;
    }

    public void setAppbarColor(int color) {
        int colorFrom = ViewUtil.getViewBackgroundColor(appbar);
        int colorTo = color;
        if (colorFrom == colorTo) return;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(400); // milliseconds
        colorAnimation.addUpdateListener(
                animator -> {
                    int background = (int) animator.getAnimatedValue();
                    appbar.setBackgroundColor(background);
                    toolbar.setBackgroundColor(background);
                }
        );
        colorAnimation.start();
    }

    private void setUpViews() {
        setUpHeights();
        setUpOnClicks();
        setUpBounceScrollView();
        setUpQueueView();
        setUpRecentlyView();
        setUpNewView();
        checkIsEmpty();
    }


    private void setUpHeights() {
        int displayHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams params;

        // Set up header height
        params = header.getLayoutParams();
        params.height = displayHeight / 3;
        header.setLayoutParams(params);

        // Set up poster image height
        params = poster.getLayoutParams();
        params.height = (int) (displayHeight / 1.5f);
        poster.setLayoutParams(params);

        // Set up posterGradient height
        posterGradient.setLayoutParams(params);

        // Set up posterGradient gradient
        //create a new gradient color
        int[] colors = {
                Color.TRANSPARENT , Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
                PhonographColorUtil.getWindowColor(activity)};
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, colors);

        posterGradient.setBackground(gd);
    }

    private  void setUpOnClicks() {
        queueParent.setOnClickListener(view -> {
            activity.setMusicChooser(R.id.nav_queue);
        });
        openQueueButton.setOnClickListener(view -> {
            activity.setMusicChooser(R.id.nav_queue);
        });
        recentlyParent.setOnClickListener(view -> {
            NavigationUtil.goToPlaylist(activity, new HistoryPlaylist(activity));
        });
        newlyParent.setOnClickListener(view -> {
            NavigationUtil.goToPlaylist(activity, new LastAddedPlaylist(activity));
        });
        shuffleBtn.setOnClickListener(view -> {
            MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(activity), true);
        });
    }

    private void setUpBounceScrollView() {
        final int displayHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        final int statusBarHeight = Util.getStatusBarHeight(activity);
        final int appbarHeight = toolbar.getLayoutParams().height;


        // get real header height
        final float headerHeight = header.getLayoutParams().height - appbarHeight - statusBarHeight;

        final int transparentColor = transparentColor();
        AtomicBoolean isStatusFlat = new AtomicBoolean(false);
        AtomicBoolean isAppbarFlat = new AtomicBoolean(false);

        bounceScrollView.setOnScrollChangeListener(
                (BounceScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                    // Scroll poster
                    posterParent.setY(
                            // for quality cast to int
                            (int) (-scrollY / (displayHeight * 2 / (float) poster.getLayoutParams().height))
                    );

                    // Scroll appbar
                    if (scrollY > headerHeight + appbarHeight && !isAppbarFlat.get()) {
                        setAppbarColor(ThemeStore.primaryColor(activity));
                        appbar.setElevation(8);
                        isAppbarFlat.set(true);
                    }
                    if (scrollY > headerHeight) {
                        if (!isStatusFlat.get()) {
                            activity.setStatusBarColorAuto();
                            isStatusFlat.set(true);
                        }
                        if (scrollY > oldScrollY) {
                            appbar.setY(
                                    Math.max(
                                            -appbarHeight, appbar.getY() + (oldScrollY - scrollY)
                                    )
                            );
                            shuffleBtn.hide();
                        } else {
                            appbar.setY(
                                    Math.min(
                                            0, appbar.getY() + (oldScrollY - scrollY)
                                    )
                            );
                            shuffleBtn.show();
                        }
                    } else {
                        if (isStatusFlat.get()) {
                            setAppbarColor(transparentColor);
                            activity.setStatusBarColor(transparentColor);
                            appbar.setElevation(0);
                            isStatusFlat.set(false);
                            isAppbarFlat.set(false);
                        }
                        appbar.setY(0);
                    }
                }
        );


        // zooming poster in over scroll
        final ViewGroup.LayoutParams params = poster.getLayoutParams();
        final int width = params.width;
        final int height = params.height;
        bounceScrollView.setOnOverScrollListener(
                (fromStart, overScrolledDistance) -> {
                    final float scale = 1 + (overScrolledDistance / (float) displayHeight);
                    final ViewGroup.LayoutParams mParams =
                            new FrameLayout.LayoutParams(width, (int) (height * scale));
                    poster.setLayoutParams(mParams);
                    posterParent.setLayoutParams(mParams);
                    posterGradient.setLayoutParams(mParams);
                }
        );
    }

    private void setUpQueueView() {
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        queueLayoutManager = getLinearLayoutManager();
        queueView.setLayoutManager(queueLayoutManager);
        queueAdapter = new HomeAdapter(
                activity,
                MusicPlayerRemote.getPlayingQueue(),
                MusicPlayerRemote.getPosition(),
                R.layout.item_card_home,
                null,
                false,
                true);
        queueView.setAdapter(queueAdapter);
        queueView.setItemAnimator(animator);
        queueLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition(), 0);
    }

    private void setUpRecentlyView() {
        recentlyLayoutManager = getGridLayoutManager();
        recentlyView.setLayoutManager(recentlyLayoutManager);
        recentlyAdapter = new HomeAdapter(
                activity,
                TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(activity),
                0,
                R.layout.item_card_home,
                getGridSize() * 2,
                false,
                false);
        recentlyView.setAdapter(recentlyAdapter);
    }

    private void setUpNewView() {
        newLayoutManager = getGridLayoutManager();
        newView.setLayoutManager(newLayoutManager);
        newAdapter = new HomeAdapter(
                activity,
                LastAddedLoader.getLastAddedSongs(activity),
                0,
                R.layout.item_card_home,
                getGridSize() * 3,
                false,
                false);
        newView.setAdapter(newAdapter);
    }

    private GridLayoutManager getGridLayoutManager() {
        final int size = getGridSize();
        return new GridLayoutManager(activity, size) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = getWidth() / size;
                lp.height = (int) (lp.width * 1.5);
                return super.checkLayoutParams(lp);
            }
        };
    }

    private LinearLayoutManager getLinearLayoutManager() {
        final int size = getGridSize();
        return new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                lp.width = getWidth() / size;
                lp.height = (int) (lp.width * 1.5);
                return super.checkLayoutParams(lp);
            }
        };
    }

    private int getGridSize() {
        return getResources().getInteger(R.integer.home_grid_columns);
    }

    class QueueListener implements MusicServiceEventListener {

        QueueListener() {
            updatePoster();
        }

        @Override
        public void onServiceConnected() {
            updatePoster();
            updateQueue();
            checkIsEmpty();
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
            updatePoster();
            updateQueue();
        }

        @Override
        public void onPlayStateChanged() {
            updateQueue();
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

        private void updatePoster() {
            if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
                Song song = MusicPlayerRemote.getCurrentSong();
//                if (navigationDrawerHeader == null) {
//                    navigationDrawerHeader = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
//                    //noinspection ConstantConditions
//                    navigationDrawerHeader.setOnClickListener(v -> {
//                        drawerLayout.closeDrawers();
//                        if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
//                            expandPanel();
//                        }
//                    });
//                }
//                ((TextView) navigationDrawerHeader.findViewById(R.id.title)).setText(song.title);
//                ((TextView) navigationDrawerHeader.findViewById(R.id.text)).setText(MusicUtil.getSongInfoString(song));
                SongGlideRequest.Builder.from(Glide.with(activity), song)
                        .build()
                        .into(poster);
            } else {
//                if (navigationDrawerHeader != null) {
//                    navigationView.removeHeaderView(navigationDrawerHeader);
//                    navigationDrawerHeader = null;
//                }
            }
        }

        private void updateQueue() {
            queueAdapter.swapDataSet(MusicPlayerRemote.getPlayingQueue(), MusicPlayerRemote.getPosition());
            resetToCurrentPosition();
        }

        private void resetToCurrentPosition() {
            if (queueAdapter.getItemCount() == 0) return;
            queueView.stopScroll();

            final int from = queueLayoutManager.findFirstVisibleItemPosition();
            final int to = MusicPlayerRemote.getPosition();
            final int delta = Math.abs(to - from);

            final int limit = 150;
            if (delta > limit) {
                queueLayoutManager.scrollToPosition(
                        to + (to > from ? -limit : limit)
                );
            }


            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(activity) {
                @Override
                protected int getHorizontalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_ANY;
                }

                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    if (delta < 20) {
                        return super.calculateSpeedPerPixel(displayMetrics) * 5;
                    } else {
                        return super.calculateSpeedPerPixel(displayMetrics);
                    }
                }


            };
            smoothScroller.setTargetPosition(to);
            queueLayoutManager.startSmoothScroll(smoothScroller);
        }
    }

    private void checkIsEmpty() {
        if (empty != null) {
            empty.setVisibility(
                    (queueAdapter == null || queueAdapter.getItemCount() == 0) &
                    (recentlyAdapter == null || recentlyAdapter.getItemCount() == 0) &
                    (newAdapter == null || newAdapter.getItemCount() == 0)
                            ? View.VISIBLE : View.GONE
            );
        }
    }

}
