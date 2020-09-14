package com.o4x.musical.ui.fragments.player.flat;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.o4x.musical.R;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.fragments.player.AbsPlayerFragment;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.Util;
import com.o4x.musical.util.ViewUtil;

public class FlatPlayerFragment extends AbsPlayerFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Util.isLandscape(getResources())) {
            impl = new LandscapeImpl(this);
        } else {
            impl = new PortraitImpl(this);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_player_flat;
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    private static abstract class BaseImpl implements Impl {
        protected FlatPlayerFragment fragment;

        public BaseImpl(FlatPlayerFragment fragment) {
            this.fragment = fragment;
        }

        public AnimatorSet createDefaultColorChangeAnimatorSet(int newColor) {
            Animator backgroundAnimator = ViewUtil.createBackgroundColorTransition(fragment.playbackControlsFragment.getView(), fragment.lastColor, newColor);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(backgroundAnimator);

            animatorSet.setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME);
            return animatorSet;
        }

        @Override
        public void animateColorChange(int newColor) {}
    }

    @SuppressWarnings("ConstantConditions")
    private static class PortraitImpl extends BaseImpl {
        Song currentSong = Song.EMPTY_SONG;

        public PortraitImpl(FlatPlayerFragment fragment) {
            super(fragment);
        }

        @Override
        public void init() {}

        @Override
        public void setUpPanelAndAlbumCoverHeight() {
            final int availablePanelHeight = fragment.slidingUpPanelLayout.getHeight() - fragment.getView().findViewById(R.id.player_content).getHeight();
            fragment.slidingUpPanelLayout.setPanelHeight(availablePanelHeight);
        }

        @Override
        public void updateCurrentSong(Song song) {
            currentSong = song;
        }

        @Override
        public void animateColorChange(int newColor) {
            super.animateColorChange(newColor);
            createDefaultColorChangeAnimatorSet(newColor).start();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class LandscapeImpl extends BaseImpl {
        public LandscapeImpl(FlatPlayerFragment fragment) {
            super(fragment);
        }

        @Override
        public void init() {
        }

        @Override
        public void setUpPanelAndAlbumCoverHeight() {

        }

        @Override
        public void updateCurrentSong(Song song) {
            fragment.toolbar.setTitle(song.title);
            fragment.toolbar.setSubtitle(MusicUtil.getSongInfoString(song));
        }

        @Override
        public void animateColorChange(int newColor) {
            super.animateColorChange(newColor);

            AnimatorSet animatorSet = createDefaultColorChangeAnimatorSet(newColor);
            animatorSet.play(ViewUtil.createBackgroundColorTransition(fragment.toolbar, fragment.lastColor, newColor));
            animatorSet.start();
        }
    }
}
