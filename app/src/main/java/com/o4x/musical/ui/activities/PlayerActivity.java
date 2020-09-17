package com.o4x.musical.ui.activities;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.ColorInt;

import com.o4x.musical.R;
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity;
import com.o4x.musical.ui.fragments.player.AbsPlayerFragment;
import com.o4x.musical.ui.fragments.player.flat.FlatPlayerFragment;

import code.name.monkey.appthemehelper.util.ColorUtil;

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

        setDrawUnderBar();

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
        super.setNavigationBarColor(Color.TRANSPARENT);

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
        super.setLightNavigationBar(ColorUtil.INSTANCE.isColorLight(playerFragmentColor));
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