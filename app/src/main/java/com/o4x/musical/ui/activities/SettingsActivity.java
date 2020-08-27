package com.o4x.musical.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.o4x.musical.App;
import com.o4x.musical.R;
import com.o4x.musical.appshortcuts.DynamicShortcutManager;
import com.o4x.musical.misc.NonProAllowedColors;
import com.o4x.musical.ui.activities.base.AbsBaseActivity;
import com.o4x.musical.ui.fragments.settings.SettingsFragment;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AbsBaseActivity implements ColorChooserDialog.ColorCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        setDrawUnderBar();
        ButterKnife.bind(this);

        setStatusBarColorAuto();
        setNavigationBarColorAuto();
        setTaskDescriptionColorAuto();

        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        } else {
            SettingsFragment frag = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (frag != null) frag.invalidateSettings();
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                if (!App.isProVersion()) {
                    Arrays.sort(NonProAllowedColors.PRIMARY_COLORS);
                    if (Arrays.binarySearch(NonProAllowedColors.PRIMARY_COLORS, selectedColor) < 0) {
                        // color wasn't found
                        Toast.makeText(this, R.string.only_the_first_5_colors_available, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, PurchaseActivity.class));
                        return;
                    }
                }
                ThemeStore.editTheme(this)
                        .primaryColor(selectedColor)
                        .commit();
                break;
            case R.string.accent_color:
                if (!App.isProVersion()) {
                    Arrays.sort(NonProAllowedColors.ACCENT_COLORS);
                    if (Arrays.binarySearch(NonProAllowedColors.ACCENT_COLORS, selectedColor) < 0) {
                        // color wasn't found
                        Toast.makeText(this, R.string.only_the_first_5_colors_available, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, PurchaseActivity.class));
                        return;
                    }
                }
                ThemeStore.editTheme(this)
                        .accentColor(selectedColor)
                        .commit();
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).updateDynamicShortcuts();
        }
        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
