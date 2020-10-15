package com.o4x.musical.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import java.util.Collections;
import java.util.Comparator;

import code.name.monkey.appthemehelper.util.ColorUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PhonographColorUtil {
    public static int desaturateColor(int color, float ratio) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        hsv[1] = (hsv[1] / 1 * ratio) + (0.2f * (1.0f - ratio));

        return Color.HSVToColor(hsv);
    }

    @Nullable
    public static Palette generatePalette(Bitmap bitmap) {
        if (bitmap == null) return null;
        return Palette.from(bitmap).generate();
    }

    @ColorInt
    public static int getColor(@Nullable Palette palette, int fallback) {
        if (palette != null) {
            if (palette.getDominantSwatch()!= null) {
                return palette.getDominantSwatch().getRgb();
            } else if (palette.getVibrantSwatch() != null) {
                return palette.getVibrantSwatch().getRgb();
            } else if (palette.getMutedSwatch() != null) {
                return palette.getMutedSwatch().getRgb();
            } else if (palette.getDarkVibrantSwatch() != null) {
                return palette.getDarkVibrantSwatch().getRgb();
            } else if (palette.getDarkMutedSwatch() != null) {
                return palette.getDarkMutedSwatch().getRgb();
            } else if (palette.getLightVibrantSwatch() != null) {
                return palette.getLightVibrantSwatch().getRgb();
            } else if (palette.getLightMutedSwatch() != null) {
                return palette.getLightMutedSwatch().getRgb();
            } else if (!palette.getSwatches().isEmpty()) {
                return Collections.max(palette.getSwatches(), SwatchComparator.getInstance()).getRgb();
            }
        }
        return fallback;
    }

    @ColorInt
    public static int getWindowColor(@NonNull Activity activity) {
        TypedValue a = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            int color = a.data;
            return color;
        } else {
            // windowBackground is not a color, probably a drawable
            Drawable d = activity.getResources().getDrawable(a.resourceId);
            return 0;
        }
    }

    private static class SwatchComparator implements Comparator<Palette.Swatch> {
        private static SwatchComparator sInstance;

        static SwatchComparator getInstance() {
            if (sInstance == null) {
                sInstance = new SwatchComparator();
            }
            return sInstance;
        }

        @Override
        public int compare(Palette.Swatch lhs, Palette.Swatch rhs) {
            return lhs.getPopulation() - rhs.getPopulation();
        }
    }

    @ColorInt
    public static int shiftBackgroundColorForLightText(@ColorInt int backgroundColor) {
        while (ColorUtil.INSTANCE.isColorLight(backgroundColor)) {
            backgroundColor = ColorUtil.INSTANCE.darkenColor(backgroundColor);
        }
        return backgroundColor;
    }

    @ColorInt
    public static int shiftBackgroundColorForDarkText(@ColorInt int backgroundColor) {
        while (!ColorUtil.INSTANCE.isColorLight(backgroundColor)) {
            backgroundColor = ColorUtil.INSTANCE.lightenColor(backgroundColor);
        }
        return backgroundColor;
    }
}
