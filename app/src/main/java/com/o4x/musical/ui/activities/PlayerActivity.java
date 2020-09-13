package com.o4x.musical.ui.activities;

import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.PathInterpolator;

import androidx.annotation.ColorInt;

import com.o4x.musical.R;
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity;
import com.o4x.musical.ui.fragments.player.AbsPlayerFragment;
import com.o4x.musical.ui.fragments.player.flat.FlatPlayerFragment;
import com.o4x.musical.util.ViewUtil;

public class PlayerActivity extends AbsMusicServiceActivity implements FlatPlayerFragment.Callbacks {

    private int navigationBarColor;
    private int taskColor;
    private boolean lightStatusBar;

    private AbsPlayerFragment playerFragment;

    private ValueAnimator navigationBarColorAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerFragment = new FlatPlayerFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.player_fragment_container, playerFragment).commit();
        getSupportFragmentManager().executePendingTransactions();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel(); // just in case
    }

    @Override
    protected void onStart() {
        super.onStart();
        // setting fragments values
        int playerFragmentColor = playerFragment.getPaletteColor();
        this.setLightStatusBar(false);
        super.setTaskDescriptionColor(playerFragmentColor);
        super.setNavigationBarColor(playerFragmentColor);

        playerFragment.setMenuVisibility(true);
        playerFragment.setUserVisibleHint(true);
        playerFragment.onShow();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // restore values
        super.setLightStatusBar(lightStatusBar);
        super.setTaskDescriptionColor(taskColor);
        super.setNavigationBarColor(navigationBarColor);

        playerFragment.setMenuVisibility(false);
        playerFragment.setUserVisibleHint(false);
        playerFragment.onHide();
    }

    @Override
    public void onPaletteColorChanged() {
        int playerFragmentColor = playerFragment.getPaletteColor();
        super.setTaskDescriptionColor(playerFragmentColor);
        animateNavigationBarColor(playerFragmentColor);
    }

    private void animateNavigationBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel();
            navigationBarColorAnimator = ValueAnimator
                    .ofArgb(getWindow().getNavigationBarColor(), color)
                    .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME);
            navigationBarColorAnimator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
            navigationBarColorAnimator.addUpdateListener(animation -> PlayerActivity.super.setNavigationBarColor((Integer) animation.getAnimatedValue()));
            navigationBarColorAnimator.start();
        }
    }

    @Override
    public void setNavigationBarColor(int color) {
        this.navigationBarColor = color;
        if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel();
        super.setNavigationBarColor(color);
    }

    @Override
    public void setLightStatusBar(boolean enabled) {
        lightStatusBar = enabled;
        super.setLightStatusBar(enabled);
    }

    @Override
    public void setTaskDescriptionColor(@ColorInt int color) {
        this.taskColor = color;
        super.setTaskDescriptionColor(color);
    }
}