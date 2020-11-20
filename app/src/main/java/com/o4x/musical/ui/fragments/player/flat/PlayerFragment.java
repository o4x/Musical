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
import com.o4x.musical.util.Util;
import com.o4x.musical.util.ViewUtil;

public class PlayerFragment extends AbsPlayerFragment {

    public PlayerFragment() {
        super(R.layout.fragment_player);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Util.isLandscape(getResources())) {
            impl = new LandscapeImpl(this);
        } else {
            impl = new PortraitImpl(this);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private static abstract class BaseImpl implements Impl {
        protected PlayerFragment fragment;

        public BaseImpl(PlayerFragment fragment) {
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
        Song currentSong = Song.getEmptySong();

        public PortraitImpl(PlayerFragment fragment) {
            super(fragment);
        }

        @Override
        public void init() {}

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
        public LandscapeImpl(PlayerFragment fragment) {
            super(fragment);
        }

        @Override
        public void init() {
        }

        @Override
        public void updateCurrentSong(Song song) {

        }

        @Override
        public void animateColorChange(int newColor) {
            super.animateColorChange(newColor);
            createDefaultColorChangeAnimatorSet(newColor).start();
        }
    }
}
