package com.o4x.musical.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.ThemeStore.Companion.textColorPrimary
import code.name.monkey.appthemehelper.ThemeStore.Companion.textColorSecondary
import code.name.monkey.appthemehelper.ThemeStore.Companion.themeColor
import code.name.monkey.appthemehelper.util.ATHUtil.resolveColor
import code.name.monkey.appthemehelper.util.ColorUtil.withAlpha
import code.name.monkey.appthemehelper.util.NavigationViewUtil.setItemIconColors
import code.name.monkey.appthemehelper.util.NavigationViewUtil.setItemTextColors
import com.google.android.material.navigation.NavigationView
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.equalizer.EqualizerFragment
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.SearchQueryHelper
import com.o4x.musical.imageloader.universalil.UniversalIL
import com.o4x.musical.loader.AlbumLoader
import com.o4x.musical.loader.ArtistLoader
import com.o4x.musical.loader.PlaylistSongLoader
import com.o4x.musical.model.Song
import com.o4x.musical.service.MusicService
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import com.o4x.musical.ui.activities.intro.AppIntroActivity
import com.o4x.musical.ui.dialogs.ChangelogDialog
import com.o4x.musical.ui.dialogs.ScanMediaFolderChooserDialog
import com.o4x.musical.ui.fragments.mainactivity.folders.FoldersFragment
import com.o4x.musical.ui.fragments.mainactivity.home.HomeFragment.Companion.newInstance
import com.o4x.musical.ui.fragments.mainactivity.library.LibraryFragment
import com.o4x.musical.ui.fragments.mainactivity.queue.QueueFragment
import com.o4x.musical.util.PreferenceUtil.introShown
import com.o4x.musical.util.PreferenceUtil.lastChangelogVersion
import com.o4x.musical.util.PreferenceUtil.setIntroShown
import java.util.*

class MainActivity : AbsMusicPanelActivity() {

    @JvmField
    @BindView(R.id.navigation_view)
    var navigationView: NavigationView? = null
    @JvmField
    @BindView(R.id.drawer_layout)
    var drawerLayout: DrawerLayout? = null

    var currentFragment: MainActivityFragmentCallbacks? = null

    private var blockRequestPermissions = false


    override fun createContentView(): View {
        @SuppressLint("InflateParams") val contentView =
            layoutInflater.inflate(R.layout.activity_main_drawer_layout, null)
        val drawerContent = contentView.findViewById<ViewGroup>(R.id.drawer_content_container)
        drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main_content))

        // To apply WindowInsets only for navigation view, not content and it's very important.
        contentView.setOnApplyWindowInsetsListener { view: View, windowInsets: WindowInsets ->
            view.findViewById<View>(R.id.navigation_view).onApplyWindowInsets(windowInsets)
            view.findViewById<View>(R.id.drawer_content_container).setPadding(
                0, 0, 0, windowInsets.systemWindowInsetBottom)
            windowInsets
        }
        return contentView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDrawUnderBar()
        ButterKnife.bind(this)
        setUpDrawerLayout()
        if (savedInstanceState == null) {
            setMusicChooser(R.id.nav_home)
        } else {
            restoreCurrentFragment()
        }
        if (!checkShowIntro()) {
            showChangelog()
        }

        // called if the cached value was outdated (should be a rare event)
        App.setOnProVersionChangedListener { checkSetUpPro() }
        UniversalIL.initImageLoader(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        App.setOnProVersionChangedListener(null)
    }

    fun setMusicChooser(id: Int) {
        var id = id
        if (!App.isProVersion() && id == R.id.nav_folders) {
            Toast.makeText(this, R.string.folder_view_is_a_pro_feature, Toast.LENGTH_LONG).show()
            startActivity(Intent(this, PurchaseActivity::class.java))
            id = R.id.nav_library
        }
        navigationView!!.setCheckedItem(id)
        when (id) {
            R.id.nav_home -> setCurrentFragment(newInstance())
            R.id.nav_queue -> setCurrentFragment(QueueFragment.newInstance())
            R.id.nav_library -> setCurrentFragment(LibraryFragment.newInstance())
            R.id.nav_folders -> setCurrentFragment(FoldersFragment.newInstance(this))
            R.id.nav_eq -> setCurrentFragment(
                EqualizerFragment.newBuilder()
                    .setthemeColor(Color.parseColor("#4caf50"))
                    .setAudioSessionId(MusicPlayerRemote.getAudioSessionId())
                    .build()
            )
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, null)
            .commit()
        currentFragment = fragment as MainActivityFragmentCallbacks
    }

    private fun restoreCurrentFragment() {
        currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as MainActivityFragmentCallbacks?
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

    private fun setUpNavigationView() {
        val themeColor = themeColor(this)
        setItemIconColors(navigationView!!,
            resolveColor(this, R.attr.iconColor, textColorSecondary(this)),
            themeColor)
        setItemTextColors(navigationView!!, textColorPrimary(this), themeColor)
        val stateListDrawable = navigationView!!.itemBackground as StateListDrawable?
        val layerDrawable = stateListDrawable!!.getStateDrawable(0) as LayerDrawable?
        val rectangle = layerDrawable!!.findDrawableByLayerId(R.id.rectangle) as GradientDrawable
        val rectangleRadius =
            layerDrawable.findDrawableByLayerId(R.id.rectangle_radius) as GradientDrawable
        rectangle.setColor(themeColor)
        rectangleRadius.setColor(withAlpha(textColorPrimary(this), 0.4f))
        checkSetUpPro()
        navigationView!!.setNavigationItemSelectedListener { menuItem: MenuItem ->
            drawerLayout!!.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_home -> Handler().postDelayed(
                    { setMusicChooser(R.id.nav_home) }, 200)
                R.id.nav_queue -> Handler().postDelayed({ setMusicChooser(R.id.nav_queue) }, 200)
                R.id.nav_library -> Handler().postDelayed({ setMusicChooser(R.id.nav_library) },
                    200)
                R.id.nav_folders -> Handler().postDelayed({ setMusicChooser(R.id.nav_folders) },
                    200)
                R.id.nav_eq -> {
                    Handler().postDelayed({ setMusicChooser(R.id.nav_eq) }, 200)
                    Handler().postDelayed({
                        startActivity(Intent(this@MainActivity,
                            PurchaseActivity::class.java))
                    }, 200)
                }
                R.id.buy_pro -> Handler().postDelayed({
                    startActivity(Intent(this@MainActivity,
                        PurchaseActivity::class.java))
                }, 200)
                R.id.action_scan -> Handler().postDelayed({
                    val dialog = ScanMediaFolderChooserDialog.create()
                    dialog.show(supportFragmentManager, "SCAN_MEDIA_FOLDER_CHOOSER")
                }, 200)
                R.id.nav_settings -> Handler().postDelayed({
                    startActivity(Intent(this@MainActivity,
                        SettingsActivity::class.java))
                }, 200)
            }
            true
        }
    }

    private fun checkSetUpPro() {
        navigationView!!.menu.setGroupVisible(R.id.navigation_drawer_menu_category_buy_pro,
            !App.isProVersion())
    }

    private fun setUpDrawerLayout() {
        setUpNavigationView()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handlePlaybackIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout!!.isDrawerOpen(navigationView!!)) {
                drawerLayout!!.closeDrawer(navigationView!!)
            } else {
                drawerLayout!!.openDrawer(navigationView!!)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleBackPress(): Boolean {
        if (drawerLayout!!.isDrawerOpen(navigationView!!)) {
            drawerLayout!!.closeDrawers()
            return true
        }
        return super.handleBackPress() || currentFragment != null && currentFragment!!.handleBackPress()
    }

    private fun handlePlaybackIntent(intent: Intent?) {
        if (intent == null) {
            return
        }
        val uri = intent.data
        val mimeType = intent.type
        var handled = false
        if (intent.action != null && intent.action == MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) {
            val songs = SearchQueryHelper.getSongs(this, intent.extras!!)
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
        stringKey: String
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

    companion object {
        val TAG = MainActivity::class.java.simpleName
        const val APP_INTRO_REQUEST = 100
    }
}