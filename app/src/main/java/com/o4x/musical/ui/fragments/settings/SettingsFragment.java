package com.o4x.musical.ui.fragments.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.o4x.musical.App;
import com.o4x.musical.R;
import com.o4x.musical.appshortcuts.DynamicShortcutManager;
import com.o4x.musical.preferences.BlacklistPreference;
import com.o4x.musical.preferences.BlacklistPreferenceDialog;
import com.o4x.musical.preferences.LibraryPreference;
import com.o4x.musical.preferences.LibraryPreferenceDialog;
import com.o4x.musical.preferences.NowPlayingScreenPreference;
import com.o4x.musical.preferences.NowPlayingScreenPreferenceDialog;
import com.o4x.musical.ui.activities.PurchaseActivity;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.util.PreferenceUtil;

public class SettingsFragment extends ATEPreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static void setSummary(@NonNull Preference preference) {
        setSummary(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }

    private static void setSummary(Preference preference, @NonNull Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);
        } else {
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_library);
        addPreferencesFromResource(R.xml.pref_colors);
        addPreferencesFromResource(R.xml.pref_notification);
        addPreferencesFromResource(R.xml.pref_now_playing_screen);
        addPreferencesFromResource(R.xml.pref_images);
        addPreferencesFromResource(R.xml.pref_lockscreen);
        addPreferencesFromResource(R.xml.pref_audio);
        addPreferencesFromResource(R.xml.pref_playlists);
        addPreferencesFromResource(R.xml.pref_blacklist);
    }

    @Nullable
    @Override
    public DialogFragment onCreatePreferenceDialog(Preference preference) {
        if (preference instanceof NowPlayingScreenPreference) {
            return NowPlayingScreenPreferenceDialog.newInstance();
        } else if (preference instanceof BlacklistPreference) {
            return BlacklistPreferenceDialog.newInstance();
        } else if (preference instanceof LibraryPreference) {
            return LibraryPreferenceDialog.newInstance();
        }
        return super.onCreatePreferenceDialog(preference);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setPadding(0, 0, 0, 0);
        invalidateSettings();
        PreferenceUtil.getInstance(getActivity()).registerOnSharedPreferenceChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferenceUtil.getInstance(getActivity()).unregisterOnSharedPreferenceChangedListener(this);
    }

    public void invalidateSettings() {
        final Preference generalTheme = findPreference("general_theme");
        setSummary(generalTheme);
        generalTheme.setOnPreferenceChangeListener((preference, o) -> {
            String themeName = (String) o;
            if (themeName.equals("black") && !App.isProVersion()) {
                Toast.makeText(getActivity(), R.string.black_theme_is_a_pro_feature, Toast.LENGTH_LONG).show();
                startActivity(new Intent(getContext(), PurchaseActivity.class));
                return false;
            }

            setSummary(generalTheme, o);

            ThemeStore.markChanged(getActivity());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                // Set the new theme so that updateAppShortcuts can pull it
                getActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue(themeName));
                new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
            }

            getActivity().recreate();
            return true;
        });

        final Preference autoDownloadImagesPolicy = findPreference("auto_download_images_policy");
        setSummary(autoDownloadImagesPolicy);
        autoDownloadImagesPolicy.setOnPreferenceChangeListener((preference, o) -> {
            setSummary(autoDownloadImagesPolicy, o);
            return true;
        });

        final ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
        final int primaryColor = ThemeStore.primaryColor(getActivity());
        primaryColorPref.setColor(primaryColor, ColorUtil.darkenColor(primaryColor));
        primaryColorPref.setOnPreferenceClickListener(preference -> {
            new ColorChooserDialog.Builder(getActivity(), R.string.primary_color)
                    .accentMode(false)
                    .allowUserColorInput(true)
                    .allowUserColorInputAlpha(false)
                    .preselect(primaryColor)
                    .show(getActivity());
            return true;
        });

        final ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
        final int accentColor = ThemeStore.accentColor(getActivity());
        accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor));
        accentColorPref.setOnPreferenceClickListener(preference -> {
            new ColorChooserDialog.Builder(getActivity(), R.string.accent_color)
                    .accentMode(true)
                    .allowUserColorInput(true)
                    .allowUserColorInputAlpha(false)
                    .preselect(accentColor)
                    .show(getActivity());
            return true;
        });

        TwoStatePreference colorNavBar = (TwoStatePreference) findPreference("should_color_navigation_bar");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            colorNavBar.setVisible(false);
        } else {
            colorNavBar.setChecked(ThemeStore.coloredNavigationBar(getActivity()));
            colorNavBar.setOnPreferenceChangeListener((preference, newValue) -> {
                ThemeStore.editTheme(getActivity())
                        .coloredNavigationBar((Boolean) newValue)
                        .commit();
                getActivity().recreate();
                return true;
            });
        }

        final TwoStatePreference classicNotification = (TwoStatePreference) findPreference("classic_notification");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            classicNotification.setVisible(false);
        } else {
            classicNotification.setChecked(PreferenceUtil.getInstance(getActivity()).classicNotification());
            classicNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                // Save preference
                PreferenceUtil.getInstance(getActivity()).setClassicNotification((Boolean) newValue);
                return true;
            });
        }

        final TwoStatePreference coloredNotification = (TwoStatePreference) findPreference("colored_notification");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            coloredNotification.setEnabled(PreferenceUtil.getInstance(getActivity()).classicNotification());
        } else {
            coloredNotification.setChecked(PreferenceUtil.getInstance(getActivity()).coloredNotification());
            coloredNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                // Save preference
                PreferenceUtil.getInstance(getActivity()).setColoredNotification((Boolean) newValue);
                return true;
            });
        }

        final TwoStatePreference colorAppShortcuts = (TwoStatePreference) findPreference("should_color_app_shortcuts");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            colorAppShortcuts.setVisible(false);
        } else {
            colorAppShortcuts.setChecked(PreferenceUtil.getInstance(getActivity()).coloredAppShortcuts());
            colorAppShortcuts.setOnPreferenceChangeListener((preference, newValue) -> {
                // Save preference
                PreferenceUtil.getInstance(getActivity()).setColoredAppShortcuts((Boolean) newValue);

                // Update app shortcuts
                new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();

                return true;
            });
        }

        final Preference equalizer = findPreference("equalizer");
        if (!hasEqualizer()) {
            equalizer.setEnabled(false);
            equalizer.setSummary(getResources().getString(R.string.no_equalizer));
        }
        equalizer.setOnPreferenceClickListener(preference -> {
            NavigationUtil.openEqualizer(getActivity());
            return true;
        });

        updateNowPlayingScreenSummary();
    }

    private boolean hasEqualizer() {
        final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        PackageManager pm = getActivity().getPackageManager();
        ResolveInfo ri = pm.resolveActivity(effects, 0);
        return ri != null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.NOW_PLAYING_SCREEN_ID:
                updateNowPlayingScreenSummary();
                break;
            case PreferenceUtil.CLASSIC_NOTIFICATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    findPreference("colored_notification").setEnabled(sharedPreferences.getBoolean(key, false));
                }
                break;
        }
    }

    private void updateNowPlayingScreenSummary() {
        findPreference("now_playing_screen_id").setSummary(PreferenceUtil.getInstance(getActivity()).getNowPlayingScreen().titleRes);
    }
}