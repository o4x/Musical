package com.o4x.musical.ui.activities


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.ThemeStore.Companion.textColorPrimary
import code.name.monkey.appthemehelper.ThemeStore.Companion.themeColor
import code.name.monkey.appthemehelper.util.ColorUtil.withAlpha
import code.name.monkey.appthemehelper.util.NavigationViewUtil.setItemTextColors
import code.name.monkey.retromusic.extensions.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.o4x.musical.R
import com.o4x.musical.extensions.surfaceColor
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.SearchQueryHelper.getSongs
import com.o4x.musical.imageloader.universalil.loader.UniversalIL
import com.o4x.musical.model.Song
import com.o4x.musical.repository.PlaylistSongsLoader
import com.o4x.musical.service.MusicService
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import com.o4x.musical.ui.activities.intro.PermissionActivity
import com.o4x.musical.ui.dialogs.ChangelogDialog
import com.o4x.musical.util.PreferenceUtil.lastChangelogVersion
import com.o4x.musical.views.BreadCrumbLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_drawer_layout.*
import kotlinx.android.synthetic.main.search_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class MainActivity : AbsMusicPanelActivity() {

    private var blockRequestPermissions = false

    lateinit var navController: NavController

    lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ButterKnife.bind(this)

        setDrawUnderStatusBar()
        setNavigationBarColorAuto()
        setLightNavigationBar(true)
        setStatusBarColor(Color.TRANSPARENT)

        setupNavController()
        setupToolbar()
        setUpNavigationView()

        if (!hasPermissions()) {
            val myIntent = Intent(
                this,
                PermissionActivity::class.java
            )

            this.startActivity(myIntent)
        }

        // called if the cached value was outdated (should be a rare event)
        UniversalIL.initImageLoader(this)
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    override fun createContentView(): View {
        @SuppressLint("InflateParams") val contentView =
            layoutInflater.inflate(R.layout.activity_main_drawer_layout, null)
        val drawerContent = contentView.findViewById<ViewGroup>(R.id.drawer_content_container)
        drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main))

        return contentView
    }

    private fun setupToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
    }


    fun setMusicChooser(id: Int) {

        if (id == navigation_view.checkedItem?.itemId ) {
            return
        }

        when (id) {
            R.id.nav_home -> navController.popBackStack()
            R.id.nav_queue -> navController.navigate(R.id.action_to_queue)
            R.id.nav_library -> navController.navigate(R.id.action_to_library)
            R.id.nav_folders -> navController.navigate(R.id.action_to_folders)
            R.id.nav_eq -> navController.navigate(R.id.action_to_equalizer)
        }
    }

    fun openSearch() {
        navController.navigate(R.id.action_to_search)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_INTRO_REQUEST) {
            blockRequestPermissions = false
            if (!hasPermissions()) {
                requestPermissions()
            }
        }
    }

    override fun requestPermissions() {
        if (!blockRequestPermissions) super.requestPermissions()
    }


    @SuppressLint("NewApi")
    private fun setUpNavigationView() {
        toggle = object : ActionBarDrawerToggle(
            this,
            drawer_layout,
            R.string.open,
            R.string.close
        ) {}
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val themeColor = themeColor(this)

        navigation_view.setBackgroundColor(surfaceColor())

        setItemTextColors(navigation_view, textColorPrimary(this), themeColor)

        val stateListDrawable = navigation_view.itemBackground as StateListDrawable
        val layerDrawable = stateListDrawable.getStateDrawable(0) as LayerDrawable
        val rectangle = layerDrawable.findDrawableByLayerId(R.id.rectangle) as GradientDrawable
        val rectangleRadius =
            layerDrawable.findDrawableByLayerId(R.id.rectangle_radius) as GradientDrawable

        rectangle.setColor(themeColor)
        rectangleRadius.setColor(withAlpha(themeColor, 0.2f))

        navigation_view.setNavigationItemSelectedListener { menuItem: MenuItem ->
            drawer_layout.closeDrawers()
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    when (menuItem.itemId) {
                        R.id.nav_home -> setMusicChooser(R.id.nav_home)
                        R.id.nav_queue -> setMusicChooser(R.id.nav_queue)
                        R.id.nav_library -> setMusicChooser(R.id.nav_library)
                        R.id.nav_folders -> setMusicChooser(R.id.nav_folders)
                        R.id.nav_eq -> setMusicChooser(R.id.nav_eq)
                        R.id.buy_pro -> startActivity(
                            Intent(this@MainActivity, PurchaseActivity::class.java)
                        )
                        R.id.action_scan -> {
//                            val dialog = ScanMediaFolderChooserDialog.create()
//                            dialog.show(supportFragmentManager, "SCAN_MEDIA_FOLDER_CHOOSER")
                        }
                        R.id.nav_settings -> startActivity(
                            Intent(this@MainActivity, SettingsActivity::class.java)
                        )
                    }
                },
                200
            )
            false
        }
    }

    fun setDrawerEnabled(enabled: Boolean) {
        val lockMode: Int =
            if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        drawer_layout.setDrawerLockMode(lockMode)
        toggle.isDrawerIndicatorEnabled = enabled
        toggle.syncState()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handlePlaybackIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (navController.currentDestination?.id == R.id.search)
                return false
            if (drawer_layout.isDrawerOpen(navigation_view)) {
                drawer_layout.closeDrawer(navigation_view)
            } else {
                drawer_layout.openDrawer(navigation_view)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleBackPress(): Boolean {
        if (drawer_layout.isDrawerOpen(navigation_view)) {
            drawer_layout.closeDrawers()
            return true
        }
        return super.handleBackPress() || navController.popBackStack()
    }

    private fun handlePlaybackIntent(intent: Intent) {
        lifecycleScope.launch(Dispatchers.IO) {
            val uri: Uri? = intent.data
            val mimeType: String? = intent.type
            var handled = false
            if (intent.action != null &&
                intent.action == MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH
            ) {
                val songs: List<Song> = getSongs(intent.extras!!)
                if (MusicPlayerRemote.shuffleMode == MusicService.SHUFFLE_MODE_SHUFFLE) {
                    MusicPlayerRemote.openAndShuffleQueue(songs, true)
                } else {
                    MusicPlayerRemote.openQueue(songs, 0, true)
                }
                handled = true
            }
            if (uri != null && uri.toString().isNotEmpty()) {
                MusicPlayerRemote.playFromUri(uri)
                handled = true
            } else if (MediaStore.Audio.Playlists.CONTENT_TYPE == mimeType) {
                val id = parseLongFromIntent(intent, "playlistId", "playlist")
                if (id >= 0L) {
                    val position: Int = intent.getIntExtra("position", 0)
                    val songs: List<Song> = PlaylistSongsLoader.getPlaylistSongList(get(), id)
                    MusicPlayerRemote.openQueue(songs, position, true)
                    handled = true
                }
            } else if (MediaStore.Audio.Albums.CONTENT_TYPE == mimeType) {
                val id = parseLongFromIntent(intent, "albumId", "album")
                if (id >= 0L) {
                    val position: Int = intent.getIntExtra("position", 0)
                    val songs = libraryViewModel.albumById(id).songs
                    MusicPlayerRemote.openQueue(
                        songs,
                        position,
                        true
                    )
                    handled = true
                }
            } else if (MediaStore.Audio.Artists.CONTENT_TYPE == mimeType) {
                val id = parseLongFromIntent(intent, "artistId", "artist")
                if (id >= 0L) {
                    val position: Int = intent.getIntExtra("position", 0)
                    val songs: List<Song> = libraryViewModel.artistById(id).songs
                    MusicPlayerRemote.openQueue(
                        songs,
                        position,
                        true
                    )
                    handled = true
                }
            }
            if (handled) {
                setIntent(Intent())
            }
        }

    }

    private fun parseLongFromIntent(
        intent: Intent, longKey: String,
        stringKey: String
    ): Long {
        var id = intent.getLongExtra(longKey, -1)
        if (id < 0) {
            val idString = intent.getStringExtra(stringKey)
            if (idString != null) {
                try {
                    id = idString.toLong()
                } catch (e: NumberFormatException) {
                    println(e.message)
                }
            }
        }
        return id
    }

    private fun showChangelog() {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val currentVersion = pInfo.versionCode
            if (currentVersion != lastChangelogVersion) {
                ChangelogDialog.create().show(supportFragmentManager, "CHANGE_LOG_DIALOG")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    interface MainActivityFragmentCallbacks {
        fun handleBackPress(): Boolean
    }

    private fun setupNavController() {
        navController = findNavController(R.id.fragment_container)
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home -> navigation_view.setCheckedItem(R.id.nav_home)
                R.id.queue -> navigation_view.setCheckedItem(R.id.nav_queue)
                R.id.library -> navigation_view.setCheckedItem(R.id.nav_library)
                R.id.folders -> navigation_view.setCheckedItem(R.id.nav_folders)
                R.id.equalizer -> navigation_view.setCheckedItem(R.id.nav_eq)
            }
        }
    }

    val appbar: AppBarLayout
        get() = main_appbar

    val toolbar: MaterialToolbar
        get() = main_toolbar

    val search: LinearLayout
        get() = main_search

    val tabs: TabLayout
        get() = main_tabs

    val bread_crumbs: BreadCrumbLayout
        get() = main_bread_crumbs

    companion object {
        val TAG = MainActivity::class.java.simpleName
        const val APP_INTRO_REQUEST = 100
    }
}