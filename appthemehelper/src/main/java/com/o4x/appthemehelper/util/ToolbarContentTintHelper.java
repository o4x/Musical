package com.o4x.appthemehelper.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.view.menu.BaseMenuPresenter;
import androidx.appcompat.view.menu.ListMenuItemView;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.view.menu.MenuPresenter;
import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.ToolbarWidgetWrapper;
import androidx.core.graphics.drawable.DrawableCompat;
import com.o4x.appthemehelper.R;
import com.o4x.appthemehelper.ThemeStore;
import com.o4x.appthemehelper.extensions.ColorExtKt;

import java.lang.reflect.Field;
import java.util.ArrayList;

public final class ToolbarContentTintHelper {

    public static class InternalToolbarContentTintUtil {

        public static final class SearchViewTintUtil {

            public static void setSearchViewContentColor(View searchView, final @ColorInt int color) {
                if (searchView == null) {
                    return;
                }
                final Class<?> cls = searchView.getClass();
                try {
                    final Field mSearchSrcTextViewField = cls.getDeclaredField("mSearchSrcTextView");
                    mSearchSrcTextViewField.setAccessible(true);
                    final EditText mSearchSrcTextView = (EditText) mSearchSrcTextViewField.get(searchView);
                    mSearchSrcTextView.setTextColor(color);
                    mSearchSrcTextView.setHintTextColor(ColorUtil.INSTANCE.adjustAlpha(color, 0.5f));
                    TintHelper.setCursorTint(mSearchSrcTextView, color);

                    Field field = cls.getDeclaredField("mSearchButton");
                    tintImageView(searchView, field, color);
                    field = cls.getDeclaredField("mGoButton");
                    tintImageView(searchView, field, color);
                    field = cls.getDeclaredField("mCloseButton");
                    tintImageView(searchView, field, color);
                    field = cls.getDeclaredField("mVoiceButton");
                    tintImageView(searchView, field, color);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private SearchViewTintUtil() {
            }

            private static void tintImageView(Object target, Field field, final @ColorInt int color)
                    throws Exception {
                field.setAccessible(true);
                final ImageView imageView = (ImageView) field.get(target);
                if (imageView.getDrawable() != null) {
                    imageView
                            .setImageDrawable(
                                    TintHelper.createTintedDrawable(imageView.getDrawable(), color));
                }
            }
        }

        public static void applyOverflowMenuTint(final @NonNull Context context, final Toolbar toolbar,
                final @ColorInt int color) {
            if (toolbar == null) {
                return;
            }
            toolbar.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Field f1 = Toolbar.class.getDeclaredField("mMenuView");
                        f1.setAccessible(true);
                        ActionMenuView actionMenuView = (ActionMenuView) f1.get(toolbar);
                        Field f2 = ActionMenuView.class.getDeclaredField("mPresenter");
                        f2.setAccessible(true);

                        // Actually ActionMenuPresenter
                        BaseMenuPresenter presenter = (BaseMenuPresenter) f2.get(actionMenuView);
                        Field f3 = presenter.getClass().getDeclaredField("mOverflowPopup");
                        f3.setAccessible(true);
                        MenuPopupHelper overflowMenuPopupHelper = (MenuPopupHelper) f3.get(presenter);
                        setTintForMenuPopupHelper(context, overflowMenuPopupHelper, color);

                        Field f4 = presenter.getClass().getDeclaredField("mActionButtonPopup");
                        f4.setAccessible(true);
                        MenuPopupHelper subMenuPopupHelper = (MenuPopupHelper) f4.get(presenter);
                        setTintForMenuPopupHelper(context, subMenuPopupHelper, color);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public static void setOverflowButtonColor(@NonNull Activity activity,
                final @ColorInt int color) {
            final String overflowDescription = activity
                    .getString(androidx.appcompat.R.string.abc_action_menu_overflow_description);
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final ArrayList<View> outViews = new ArrayList<>();
                    decorView.findViewsWithText(outViews, overflowDescription,
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                    if (outViews.isEmpty()) {
                        return;
                    }
                    final AppCompatImageView overflow = (AppCompatImageView) outViews.get(0);
                    overflow.setImageDrawable(TintHelper.createTintedDrawable(overflow.getDrawable(), color));
                    ViewUtil.INSTANCE.removeOnGlobalLayoutListener(decorView, this);
                }
            });
        }

        public static void setTintForMenuPopupHelper(final @NonNull Context context,
                @Nullable MenuPopupHelper menuPopupHelper, final @ColorInt int color) {
            try {
                if (menuPopupHelper != null) {
                    final ListView listView = ((ShowableListMenu) menuPopupHelper.getPopup()).getListView();
                    listView.getViewTreeObserver()
                            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    try {
                                        Field checkboxField = ListMenuItemView.class.getDeclaredField("mCheckBox");
                                        checkboxField.setAccessible(true);
                                        Field radioButtonField = ListMenuItemView.class
                                                .getDeclaredField("mRadioButton");
                                        radioButtonField.setAccessible(true);

                                        final boolean isDark = !ColorUtil.INSTANCE.isColorLight(
                                                ATHUtil.INSTANCE
                                                        .resolveColor(context, android.R.attr.windowBackground));

                                        for (int i = 0; i < listView.getChildCount(); i++) {
                                            View v = listView.getChildAt(i);
                                            if (!(v instanceof ListMenuItemView)) {
                                                continue;
                                            }
                                            ListMenuItemView iv = (ListMenuItemView) v;

                                            CheckBox check = (CheckBox) checkboxField.get(iv);
                                            if (check != null) {
                                                TintHelper.setTint(check, color, isDark);
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    check.setBackground(null);
                                                }
                                            }

                                            RadioButton radioButton = (RadioButton) radioButtonField.get(iv);
                                            if (radioButton != null) {
                                                TintHelper.setTint(radioButton, color, isDark);
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    radioButton.setBackground(null);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    } else {
                                        //noinspection deprecation
                                        listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    }
                                }
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        public static void tintMenu(@NonNull Toolbar toolbar, @Nullable Menu menu,
                final @ColorInt int color) {
            try {
                final Field field = Toolbar.class.getDeclaredField("mCollapseIcon");
                field.setAccessible(true);
                Drawable collapseIcon = (Drawable) field.get(toolbar);
                if (collapseIcon != null) {
                    field.set(toolbar, TintHelper.createTintedDrawable(collapseIcon, color));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (menu != null && menu.size() > 0) {
                for (int i = 0; i < menu.size(); i++) {
                    final MenuItem item = menu.getItem(i);
                    if (item.getIcon() != null) {
                        item.setIcon(TintHelper.createTintedDrawable(item.getIcon(), color));
                    }
                    // Search view theming
                    if (item.getActionView() != null && (
                            item.getActionView() instanceof android.widget.SearchView || item
                                    .getActionView() instanceof androidx.appcompat.widget.SearchView)) {
                        SearchViewTintUtil.setSearchViewContentColor(item.getActionView(), color);
                    }
                }
            }
        }

        private InternalToolbarContentTintUtil() {}
    }

    private ToolbarContentTintHelper() {
    }

    @CheckResult
    @NonNull
    public static PorterDuffColorFilter createTintFilter(@ColorInt int color, boolean isDark) {
        return new PorterDuffColorFilter(color, isDark ? PorterDuff.Mode.SRC_ATOP : PorterDuff.Mode.MULTIPLY);
    }

    public static void setActionBarUpIndicatorColor(Activity activity, @ColorInt int color) {
        ActionBar actionBar = null;
        if (activity instanceof androidx.appcompat.app.AppCompatActivity) {
            actionBar = ((androidx.appcompat.app.AppCompatActivity) activity).getSupportActionBar();
        }
        if (actionBar != null) {
            setActionBarUpIndicatorColor(actionBar, color);
        }
    }

    public static void setActionBarUpIndicatorColor(@NonNull ActionBar actionBar, @ColorInt int color) {
        if (actionBar instanceof WindowDecorActionBar) {
            try {
                final Field field = ToolbarWidgetWrapper.class.getDeclaredField("mNavButtonView");
                field.setAccessible(true);
                final Field mDecorToolbarField = actionBar.getClass().getDeclaredField("mDecorToolbar");
                mDecorToolbarField.setAccessible(true);
                final Object decorToolbar = mDecorToolbarField.get(actionBar);
                final ImageButton navButton = (ImageButton) field.get(decorToolbar);
                if (navButton.getDrawable() != null) {
                    navButton.setImageDrawable(TintHelper.createTintedDrawable(navButton.getDrawable(), color));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void tintAllIcons(@NonNull Menu menu, final @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            final MenuItem item = menu.getItem(i);
            if (item.getIcon() != null) {
                item.setIcon(TintHelper.createTintedDrawable(item.getIcon(), color));
            }
        }
    }

    public static void tintAllIcons(@NonNull Toolbar toolbar, final @ColorInt int color) {
        if (toolbar.getMenu() != null) {
            tintAllIcons(toolbar.getMenu(), color);
        }
        if (toolbar.getNavigationIcon() != null) {
            toolbar.setNavigationIcon(
                TintHelper.createTintedDrawable(toolbar.getNavigationIcon(), color));
        }
        if (toolbar.getOverflowIcon() != null) {
            toolbar.setOverflowIcon(
                TintHelper.createTintedDrawable(toolbar.getOverflowIcon(), color));
        }
        if (toolbar.getCollapseIcon() != null) {
            toolbar.setCollapseIcon(
                TintHelper.createTintedDrawable(toolbar.getCollapseIcon(), color));
        }
    }

    public static void tint(@NonNull Menu menu, @ColorInt final int menuColor, @ColorInt final int subMenuColor) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getIcon() != null) {
                Drawable drawable = DrawableCompat.wrap(item.getIcon());
                DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
                DrawableCompat.setTint(drawable, menuColor);
            }
            if (item.hasSubMenu()) {
                Menu subMenu = item.getSubMenu();
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    if (subMenuItem.getIcon() != null) {
                        Drawable drawable = DrawableCompat.wrap(subMenuItem.getIcon());
                        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
                        DrawableCompat.setTint(drawable, subMenuColor);
                    }
                }
            }
        }
    }
}
