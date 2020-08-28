package com.o4x.musical.ui.activities.base;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;

import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil;
import com.o4x.musical.util.PreferenceUtil;
import com.o4x.musical.util.Util;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public abstract class AbsThemeActivity extends ATHToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getGeneralTheme());
        super.onCreate(savedInstanceState);
        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);
    }

    protected void setDrawUnderBar() {
        Util.setAllowDrawUnderBar(getWindow());
    }

    /**
     * This will set the color of the view with the id "status_bar" on KitKat and Lollipop.
     * On Lollipop if no such view is found it will set the statusbar color using the native method.
     *
     * @param color the new statusbar color (will be shifted down on Lollipop and above)
     */
    public void setStatusBarColor(int color) {
        int colorFrom = getWindow().getStatusBarColor();
        int colorTo = color;
        if (colorFrom == colorTo) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(400); // milliseconds
            colorAnimation.addUpdateListener(
                    animator -> {
                        getWindow().setStatusBarColor((int) animator.getAnimatedValue());
                        setLightStatusBarAuto((int) animator.getAnimatedValue());
                    }
            );
            colorAnimation.start();
        }
    }

    public void setStatusBarColorAuto() {
        // we don't want to use statusbar color because we are doing the color darkening on our own to support KitKat
        setStatusBarColor(ThemeStore.primaryColor(this));
    }

    public void setTaskDescriptionColor(@ColorInt int color) {
        ATH.setTaskDescriptionColor(this, color);
    }

    public void setTaskDescriptionColorAuto() {
        setTaskDescriptionColor(ThemeStore.primaryColor(this));
    }

    public void setNavigationBarColor(int color) {
        if (ThemeStore.coloredNavigationBar(this)) {
            ATH.setNavigationbarColor(this, color);
        } else {
            ATH.setNavigationbarColor(this, Color.BLACK);
        }
    }

    public void setNavigationBarColorAuto() {
        setNavigationBarColor(ThemeStore.navigationBarColor(this));
    }

    public void setLightStatusBar(boolean enabled) {
        ATH.setLightStatusbar(this, enabled);
    }

    public void setLightStatusBarAuto(int bgColor) {
        setLightStatusBar(ColorUtil.isColorLight(bgColor));
    }
}
