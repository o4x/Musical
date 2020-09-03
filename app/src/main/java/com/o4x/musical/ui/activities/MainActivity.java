package com.o4x.musical.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.o4x.musical.App;
import com.o4x.musical.R;
import com.o4x.musical.equalizer.EqualizerFragment;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.helper.SearchQueryHelper;
import com.o4x.musical.imageloader.universalil.UniversalIL;
import com.o4x.musical.loader.AlbumLoader;
import com.o4x.musical.loader.ArtistLoader;
import com.o4x.musical.loader.PlaylistSongLoader;
import com.o4x.musical.model.Song;
import com.o4x.musical.service.MusicService;
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity;
import com.o4x.musical.ui.activities.intro.AppIntroActivity;
import com.o4x.musical.ui.dialogs.ChangelogDialog;
import com.o4x.musical.ui.dialogs.ScanMediaFolderChooserDialog;
import com.o4x.musical.ui.fragments.mainactivity.folders.FoldersFragment;
import com.o4x.musical.ui.fragments.mainactivity.home.HomeFragment;
import com.o4x.musical.ui.fragments.mainactivity.library.LibraryFragment;
import com.o4x.musical.ui.fragments.mainactivity.queue.QueueFragment;
import com.o4x.musical.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.NavigationViewUtil;

public class MainActivity extends AbsMusicPanelActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int APP_INTRO_REQUEST = 100;


    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Nullable
    MainActivityFragmentCallbacks currentFragment;

    private boolean blockRequestPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderBar();
        ButterKnife.bind(this);

        setUpDrawerLayout();

        if (savedInstanceState == null) {
            setMusicChooser(PreferenceUtil.getLastMusicChooser());
        } else {
            restoreCurrentFragment();
        }

        if (!checkShowIntro()) {
            showChangelog();
        }

        App.setOnProVersionChangedListener(() -> {
            // called if the cached value was outdated (should be a rare event)
            checkSetUpPro();
            if (!App.isProVersion() && PreferenceUtil.getLastMusicChooser() == R.id.nav_folders) {
                setMusicChooser(R.id.nav_folders); // shows the purchase activity and switches to LIBRARY
            }
        });

        UniversalIL.initImageLoader(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.setOnProVersionChangedListener(null);
    }

    public void setMusicChooser(int id) {
        if (!App.isProVersion() && id == R.id.nav_folders) {
            Toast.makeText(this, R.string.folder_view_is_a_pro_feature, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, PurchaseActivity.class));
            id = R.id.nav_library;
        }

        PreferenceUtil.setLastMusicChooser(id);
        navigationView.setCheckedItem(id);
        switch (id) {
            case R.id.nav_home:
                setCurrentFragment(HomeFragment.newInstance());
                break;
            case R.id.nav_queue:
                setCurrentFragment(QueueFragment.newInstance());
                break;
            case R.id.nav_library:
                setCurrentFragment(LibraryFragment.newInstance());
                break;
            case R.id.nav_folders:
                setCurrentFragment(FoldersFragment.newInstance(this));
                break;
            case R.id.nav_eq:
                setCurrentFragment(
                        EqualizerFragment.newBuilder()
                        .setthemeColor(Color.parseColor("#4caf50"))
                        .setAudioSessionId(MusicPlayerRemote.getAudioSessionId())
                        .build()
                );
                break;
        }
    }

    private void setCurrentFragment(@SuppressWarnings("NullableProblems") Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, null).commit();
        currentFragment = (MainActivityFragmentCallbacks) fragment;
    }

    private void restoreCurrentFragment() {
        currentFragment = (MainActivityFragmentCallbacks) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_INTRO_REQUEST) {
            blockRequestPermissions = false;
            if (!hasPermissions()) {
                requestPermissions();
            }
        }
    }

    @Override
    protected void requestPermissions() {
        if (!blockRequestPermissions) super.requestPermissions();
    }

    @Override
    protected View createContentView() {
        @SuppressLint("InflateParams")
        View contentView = getLayoutInflater().inflate(R.layout.activity_main_drawer_layout, null);
        ViewGroup drawerContent = contentView.findViewById(R.id.drawer_content_container);
        drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main_content));

        // To apply WindowInsets only for navigation view, not content and it's very important.
        contentView.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            view.findViewById(R.id.navigation_view).onApplyWindowInsets(windowInsets);
            view.findViewById(R.id.drawer_content_container).setPadding(
                    0,0,0,windowInsets.getSystemWindowInsetBottom());
            return windowInsets;
        });

        return contentView;
    }



    private void setUpNavigationView() {
        int themeColor = ThemeStore.Companion.themeColor(this);
        NavigationViewUtil.INSTANCE.setItemIconColors(navigationView, ATHUtil.INSTANCE.resolveColor(this, R.attr.iconColor, ThemeStore.Companion.textColorSecondary(this)), themeColor);
        NavigationViewUtil.INSTANCE.setItemTextColors(navigationView, ThemeStore.Companion.textColorPrimary(this), themeColor);

        StateListDrawable stateListDrawable = (StateListDrawable) navigationView.getItemBackground();
        LayerDrawable layerDrawable = (LayerDrawable) stateListDrawable.getStateDrawable(0);
        GradientDrawable rectangle = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.rectangle);
        GradientDrawable rectangleRadius = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.rectangle_radius);

        rectangle.setColor(themeColor);
        rectangleRadius.setColor(ColorUtil.INSTANCE.withAlpha(ThemeStore.Companion.textColorSecondary(this), 0.1f));

        checkSetUpPro();
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.nav_home:
                    new Handler().postDelayed(() -> setMusicChooser(R.id.nav_home), 200);
                    break;
                case R.id.nav_queue:
                    new Handler().postDelayed(() -> setMusicChooser(R.id.nav_queue), 200);
                    break;
                case R.id.nav_library:
                    new Handler().postDelayed(() -> setMusicChooser(R.id.nav_library), 200);
                    break;
                case R.id.nav_folders:
                    new Handler().postDelayed(() -> setMusicChooser(R.id.nav_folders), 200);
                    break;
                case R.id.nav_eq:
                    new Handler().postDelayed(() -> setMusicChooser(R.id.nav_eq), 200);
                case R.id.buy_pro:
                    new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, PurchaseActivity.class)), 200);
                    break;
                case R.id.action_scan:
                    new Handler().postDelayed(() -> {
                        ScanMediaFolderChooserDialog dialog = ScanMediaFolderChooserDialog.create();
                        dialog.show(getSupportFragmentManager(), "SCAN_MEDIA_FOLDER_CHOOSER");
                    }, 200);
                    break;
                case R.id.nav_settings:
                    new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)), 200);
                    break;
            }
            return true;
        });
    }

    private void checkSetUpPro() {
        navigationView.getMenu().setGroupVisible(R.id.navigation_drawer_menu_category_buy_pro, !App.isProVersion());
    }

    private void setUpDrawerLayout() {
        setUpNavigationView();
    }



    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        handlePlaybackIntent(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleBackPress() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return true;
        }
        return super.handleBackPress() || (currentFragment != null && currentFragment.handleBackPress());
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        boolean handled = false;

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            final List<Song> songs = SearchQueryHelper.getSongs(this, intent.getExtras());
            if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
                MusicPlayerRemote.openAndShuffleQueue(songs, true);
            } else {
                MusicPlayerRemote.openQueue(songs, 0, true);
            }
            handled = true;
        }

        if (uri != null && uri.toString().length() > 0) {
            MusicPlayerRemote.playFromUri(uri);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "playlistId", "playlist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                List<Song> songs = new ArrayList<>(PlaylistSongLoader.getPlaylistSongList(this, id));
                MusicPlayerRemote.openQueue(songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "albumId", "album");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(AlbumLoader.getAlbum(this, id).songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            final int id = (int) parseIdFromIntent(intent, "artistId", "artist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(ArtistLoader.getArtist(this, id).getSongs(), position, true);
                handled = true;
            }
        }
        if (handled) {
            setIntent(new Intent());
        }
    }

    private long parseIdFromIntent(@NonNull Intent intent, String longKey,
                                   String stringKey) {
        long id = intent.getLongExtra(longKey, -1);
        if (id < 0) {
            String idString = intent.getStringExtra(stringKey);
            if (idString != null) {
                try {
                    id = Long.parseLong(idString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return id;
    }

    private boolean checkShowIntro() {
        if (!PreferenceUtil.introShown()) {
            PreferenceUtil.setIntroShown();
            ChangelogDialog.setChangelogRead(this);
            blockRequestPermissions = true;
            new Handler().postDelayed(() -> startActivityForResult(new Intent(MainActivity.this, AppIntroActivity.class), APP_INTRO_REQUEST), 50);
            return true;
        }
        return false;
    }

    private void showChangelog() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int currentVersion = pInfo.versionCode;
            if (currentVersion != PreferenceUtil.getLastChangelogVersion()) {
                ChangelogDialog.create().show(getSupportFragmentManager(), "CHANGE_LOG_DIALOG");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public interface MainActivityFragmentCallbacks {
        boolean handleBackPress();
    }
}
