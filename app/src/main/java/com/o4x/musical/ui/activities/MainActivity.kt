package com.o4x.musical.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import com.o4x.musical.App
import com.o4x.musical.R
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
import kotlinx.android.synthetic.main.activity_main_drawer_layout.*
import java.util.*

class MainActivity : AbsMusicPanelActivity() {

    private var blockRequestPermissions = false

    lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ButterKnife.bind(this)
        navController = findNavController(R.id.fragment_container)

        setDrawUnderBar()
        setUpNavigationView()


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
                0, 0, 0, windowInsets.systemWindowInsetBottom)
            windowInsets
        }
        return contentView
    }

    fun setMusicChooser(id: Int) {
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


    private fun setUpNavigationView() {
        navigation_view.setCheckedItem(R.id.nav_home)

        val themeColor = themeColor(this)

        setItemIconColors(navigation_view,
            resolveColor(this, R.attr.iconColor, textColorSecondary(this)),
            themeColor)
        setItemTextColors(navigation_view, textColorPrimary(this), themeColor)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val stateListDrawable = navigation_view.itemBackground as StateListDrawable
            val layerDrawable = stateListDrawable.getStateDrawable(0) as LayerDrawable
            val rectangle = layerDrawable.findDrawableByLayerId(R.id.rectangle) as GradientDrawable
            val rectangleRadius =
                layerDrawable.findDrawableByLayerId(R.id.rectangle_radius) as GradientDrawable

            rectangle.setColor(themeColor)
            rectangleRadius.setColor(withAlpha(textColorPrimary(this), 0.4f))
        }

        checkSetUpPro()
        navigation_view.setNavigationItemSelectedListener { menuItem: MenuItem ->
            drawer_layout.closeDrawers()
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