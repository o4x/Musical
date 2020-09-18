package com.o4x.musical.ui.activities


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.navigation.NavController
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.ThemeStore.Companion.textColorPrimary
import code.name.monkey.appthemehelper.ThemeStore.Companion.textColorSecondary
import code.name.monkey.appthemehelper.ThemeStore.Companion.themeColor
import code.name.monkey.appthemehelper.util.ATHUtil.resolveColor
import code.name.monkey.appthemehelper.util.ColorUtil.withAlpha
import code.name.monkey.appthemehelper.util.NavigationViewUtil.setItemIconColors
import code.name.monkey.appthemehelper.util.NavigationViewUtil.setItemTextColors
import code.name.monkey.retromusic.extensions.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.extensions.primaryColor
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.SearchQueryHelper
import com.o4x.musical.imageloader.universalil.UniversalIL
import com.o4x.musical.loader.AlbumLoader
import com.o4x.musical.loader.ArtistLoader
import com.o4x.musical.loader.PlaylistSongLoader
import com.o4x.musical.model.Song
import com.o4x.musical.service.MusicService
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import com.o4x.musical.ui.activities.intro.AppIntroActivity
import com.o4x.musical.ui.dialogs.ChangelogDialog
import com.o4x.musical.ui.dialogs.ScanMediaFolderChooserDialog
import com.o4x.musical.util.PreferenceUtil.introShown
import com.o4x.musical.util.PreferenceUtil.lastChangelogVersion
import com.o4x.musical.util.PreferenceUtil.setIntroShown
import com.o4x.musical.views.BreadCrumbLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_drawer_layout.*
import java.util.*

class MainActivity : AbsMusicPanelActivity() {

    private var blockRequestPermissions = false

    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ButterKnife.bind(this)

        setNavigationBarColorAuto()

        setupNavController()
        setDrawUnderBar()
        setupToolbar()
        setUpNavigationView()
        setStatusBarColor(Color.TRANSPARENT)


        if (!checkShowIntro()) {
            showChangelog()
        }

        // called if the cached value was outdated (should be a rare event)
        App.setOnProVersionChangedListener { checkSetUpPro() }
        UniversalIL.initImageLoader(this)
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        App.setOnProVersionChangedListener(null)
    }


    override fun createContentView(): View {
        @SuppressLint("InflateParams") val contentView =
            layoutInflater.inflate(R.layout.activity_main_drawer_layout, null)
        val drawerContent = contentView.findViewById<ViewGroup>(R.id.drawer_content_container)
        drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main))

        // To apply WindowInsets only for navigation view, not content and it's very important.
        contentView.setOnApplyWindowInsetsListener { view: View, windowInsets: WindowInsets ->
            view.findViewById<View>(R.id.navigation_view).onApplyWindowInsets(windowInsets)
            view.findViewById<View>(R.id.drawer_content_container).setPadding(
                windowInsets.systemWindowInsetLeft, 0, windowInsets.systemWindowInsetRight, windowInsets.systemWindowInsetBottom)
            windowInsets
        }
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

        navigation_view.setCheckedItem(id)
        when (id) {
            R.id.nav_home -> navController.popBackStack()
            R.id.nav_queue -> navController.navigate(R.id.action_to_queue)
            R.id.nav_library -> navController.navigate(R.id.action_to_library)
            R.id.nav_folders -> navController.navigate(R.id.action_to_folders)
//            R.id.nav_eq -> setCurrentFragment(
//                EqualizerFragment.newBuilder()
//                    .setthemeColor(Color.parseColor("#4caf50"))
//                    .setAudioSessionId(MusicPlayerRemote.getAudioSessionId())
//                    .build()
//            )
        }
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
        navigation_view.setCheckedItem(R.id.nav_home)

        val themeColor = themeColor(this)

        navigation_view.setBackgroundColor(primaryColor())

        setItemTextColors(navigation_view, textColorPrimary(this), themeColor)

        val stateListDrawable = navigation_view.itemBackground as StateListDrawable
        val layerDrawable = stateListDrawable.getStateDrawable(0) as LayerDrawable
        val rectangle = layerDrawable.findDrawableByLayerId(R.id.rectangle) as GradientDrawable
        val rectangleRadius =
            layerDrawable.findDrawableByLayerId(R.id.rectangle_radius) as GradientDrawable

        rectangle.setColor(themeColor)
        rectangleRadius.setColor(withAlpha(themeColor, 0.2f))

        checkSetUpPro()
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
                        R.id.buy_pro ->  startActivity(
                            Intent(this@MainActivity, PurchaseActivity::class.java)
                        )
                        R.id.action_scan -> {
                            val dialog = ScanMediaFolderChooserDialog.create()
                            dialog.show(supportFragmentManager, "SCAN_MEDIA_FOLDER_CHOOSER")
                        }
                        R.id.nav_settings -> startActivity(
                            Intent(this@MainActivity, SettingsActivity::class.java)
                        )
                    }
                },
                200)
            false
        }
    }

    private fun checkSetUpPro() {
        navigation_view.menu.setGroupVisible(R.id.navigation_drawer_menu_category_buy_pro,
            !App.isProVersion())
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handlePlaybackIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
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
        navigation_view.setCheckedItem(R.id.nav_home)
        return super.handleBackPress() || navController.popBackStack()
    }

    private fun handlePlaybackIntent(intent: Intent?) {
        if (intent == null) {
            return
        }
        val uri = intent.data
        val mimeType = intent.type
        var handled = false
        if (intent.action != null && intent.action == MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) {
            val songs = intent.extras?.let { SearchQueryHelper.getSongs(this, it) }
            if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
                MusicPlayerRemote.openAndShuffleQueue(songs, true)
            } else {
                MusicPlayerRemote.openQueue(songs, 0, true)
            }
            handled = true
        }
        if (uri != null && uri.toString().length > 0) {
            MusicPlayerRemote.playFromUri(uri)
            handled = true
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE == mimeType) {
            val id = parseIdFromIntent(intent, "playlistId", "playlist").toInt()
            if (id >= 0) {
                val position = intent.getIntExtra("position", 0)
                val songs: List<Song> =
                    ArrayList<Song>(PlaylistSongLoader.getPlaylistSongList(this, id))
                MusicPlayerRemote.openQueue(songs, position, true)
                handled = true
            }
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE == mimeType) {
            val id = parseIdFromIntent(intent, "albumId", "album").toInt()
            if (id >= 0) {
                val position = intent.getIntExtra("position", 0)
                MusicPlayerRemote.openQueue(AlbumLoader.getAlbum(this, id).songs, position, true)
                handled = true
            }
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE == mimeType) {
            val id = parseIdFromIntent(intent, "artistId", "artist").toInt()
            if (id >= 0) {
                val position = intent.getIntExtra("position", 0)
                MusicPlayerRemote.openQueue(ArtistLoader.getArtist(this, id).songs, position, true)
                handled = true
            }
        }
        if (handled) {
            setIntent(Intent())
        }
    }

    private fun parseIdFromIntent(
        intent: Intent, longKey: String,
        stringKey: String,
    ): Long {
        var id = intent.getLongExtra(longKey, -1)
        if (id < 0) {
            val idString = intent.getStringExtra(stringKey)
            if (idString != null) {
                try {
                    id = idString.toLong()
                } catch (e: NumberFormatException) {
                    Log.e(TAG, e.message!!)
                }
            }
        }
        return id
    }

    private fun checkShowIntro(): Boolean {
        if (!introShown()) {
            setIntroShown()
            ChangelogDialog.setChangelogRead(this)
            blockRequestPermissions = true
            Handler().postDelayed({
                startActivityForResult(Intent(this@MainActivity,
                    AppIntroActivity::class.java), APP_INTRO_REQUEST)
            }, 50)
            return true
        }
        return false
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

//        val toolbarParams: AppBarLayout.LayoutParams =
//            toolbar.layoutParams as AppBarLayout.LayoutParams
//
//        val coordinatorParams: CoordinatorLayout.LayoutParams =
//            fragment_container.layoutParams as CoordinatorLayout.LayoutParams
//
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            if (destination.id == R.id.home) {
//                appbar.elevation = 0f
//                toolbarParams.scrollFlags = 0
//                coordinatorParams.behavior = null
//            } else {
//                setStatusBarColorAuto()
//                toolbar.setBackgroundColor(primaryColor())
//                appbar.elevation = resources.getDimension(R.dimen.appbar_elevation)
//                toolbarParams.scrollFlags = (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
//                        or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
//
//                coordinatorParams.behavior = ScrollingViewBehavior()
//            }
//        }
    }

    val appbar: AppBarLayout
        get() = main_appbar

    val toolbar: MaterialToolbar
        get() = main_toolbar

    val tabs: TabLayout
        get() = main_tabs

    val bread_crumbs: BreadCrumbLayout
        get() = main_bread_crumbs

    companion object {
        val TAG = MainActivity::class.java.simpleName
        const val APP_INTRO_REQUEST = 100
    }
}