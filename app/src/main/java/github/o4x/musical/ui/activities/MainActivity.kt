package github.o4x.musical.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.o4x.appthemehelper.extensions.surfaceColor
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import github.o4x.musical.R
import github.o4x.musical.databinding.ActivityMainBinding
import github.o4x.musical.databinding.ActivityMainDrawerLayoutBinding
import github.o4x.musical.databinding.ActivityMainNavHeaderBinding
import github.o4x.musical.extensions.findNavController
import github.o4x.musical.helper.MusicPlayerRemote
import github.o4x.musical.helper.SearchQueryHelper.getSongs
import github.o4x.musical.interfaces.CabHolder
import github.o4x.musical.model.Song
import github.o4x.musical.repository.PlaylistSongsLoader
import github.o4x.musical.service.MusicService
import github.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import github.o4x.musical.views.BreadCrumbLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class MainActivity : AbsMusicPanelActivity(), CabHolder {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName

        private val popupAbleFragments: Array<Int> =
            arrayOf(R.id.search_fragment, R.id.playlist_detail_fragment, R.id.genre_detail_fragment)
    }

    private lateinit var binding: ActivityMainDrawerLayoutBinding
    private lateinit var mainBinding: ActivityMainBinding

    lateinit var navController: NavController
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var navigationHeaderBinding: ActivityMainNavHeaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDrawUnderStatusBar()
        setNavigationBarColorAuto()
        setStatusBarColorAuto()
        setLightNavigationBar(true)

        setupNavController()
        setupToolbar()
        setUpNavigationView()
    }

    override fun onResume() {
        super.onResume()
        checkVersion()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun createContentView(): View {
        binding = ActivityMainDrawerLayoutBinding.inflate(layoutInflater)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)

        val drawerContent = binding.drawerContentContainer
        val slidingPanel = wrapSlidingMusicPanel(mainBinding.root)
        drawerContent.addView(slidingPanel)

        return binding.root
    }

    private fun setupToolbar() {
        mainBinding.mainToolbar.setNavigationIcon(R.drawable.ic_menu)
        setSupportActionBar(mainBinding.mainToolbar)
    }

    fun setMusicChooser(id: Int) {
        if (id == binding.navigationView.checkedItem?.itemId) {
            return
        }

        when (id) {
            R.id.nav_home -> navController.popBackStack()
            R.id.nav_queue -> navController.navigate(R.id.action_to_queue)
            R.id.nav_library -> navController.navigate(R.id.action_to_library)
            R.id.nav_folders -> navController.navigate(R.id.action_to_folders)
            R.id.nav_timer -> navController.navigate(R.id.action_to_timer)
        }
    }

    fun openSearch() {
        navController.navigate(R.id.action_to_search)
    }

    private fun checkVersion() {
        navigationHeaderBinding.apply {
            if (false) {
                proIcon.isVisible = false
                proNext.isVisible = false
                proSummary.isVisible = false
                proTitle.text = resources.getString(R.string.musical)
                banner.setOnClickListener { }
            } else {
                proIcon.isVisible = true
                proNext.isVisible = true
                proSummary.isVisible = true
                proTitle.text = resources.getString(R.string.musical_clean)
            }
        }
    }

    private fun setUpNavigationView() {
        toggle = object : ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open,
            R.string.close
        ) {}
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setBackgroundColor(surfaceColor())
        navigationHeaderBinding = ActivityMainNavHeaderBinding.inflate(layoutInflater)
        binding.navigationView.addHeaderView(navigationHeaderBinding.root)

        var lastItem: MenuItem? = null
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                when (lastItem?.itemId) {
                    R.id.nav_home -> setMusicChooser(R.id.nav_home)
                    R.id.nav_queue -> setMusicChooser(R.id.nav_queue)
                    R.id.nav_library -> setMusicChooser(R.id.nav_library)
                    R.id.nav_folders -> setMusicChooser(R.id.nav_folders)
                    R.id.nav_timer -> setMusicChooser(R.id.nav_timer)
                    R.id.nav_settings -> navController.navigate(R.id.settings_activity)
                }
            }
        })

        binding.navigationView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            lastItem = menuItem
            binding.drawerLayout.closeDrawers()
            false
        }
    }

    fun setDrawerEnabled(enabled: Boolean) {
        val lockMode: Int =
            if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        binding.drawerLayout.setDrawerLockMode(lockMode)
        toggle.isDrawerIndicatorEnabled = enabled
        toggle.syncState()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handlePlaybackIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {

            for (id in popupAbleFragments) {
                if (navController.currentDestination?.id == id)
                    return false
            }

            if (binding.drawerLayout.isDrawerOpen(binding.navigationView)) {
                binding.drawerLayout.closeDrawer(binding.navigationView)
            } else {
                binding.drawerLayout.openDrawer(binding.navigationView)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleBackPress(): Boolean {
        if (binding.drawerLayout.isDrawerOpen(binding.navigationView)) {
            binding.drawerLayout.closeDrawers()
            return true
        }
        backPressCallbacks.forEach {
            if (it.handleBackPress()) {
                return true
            }
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

    val backPressCallbacks = mutableListOf<MainActivityFragmentCallbacks>()

    interface MainActivityFragmentCallbacks {
        fun handleBackPress(): Boolean
    }

    private fun setupNavController() {
        navController = findNavController(R.id.fragment_container)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home_fragment -> binding.navigationView.setCheckedItem(R.id.nav_home)
                R.id.queue_fragment -> binding.navigationView.setCheckedItem(R.id.nav_queue)
                R.id.library_fragment -> binding.navigationView.setCheckedItem(R.id.nav_library)
                R.id.folders_fragment -> binding.navigationView.setCheckedItem(R.id.nav_folders)
                R.id.timer_fragment -> binding.navigationView.setCheckedItem(R.id.nav_timer)
            }
        }
    }

    val appbar: AppBarLayout
        get() = mainBinding.mainAppbar

    val toolbar: MaterialToolbar
        get() = mainBinding.mainToolbar

    val search: LinearLayout
        get() = mainBinding.toolbarTitle.mainSearch

    val tabs: TabLayout
        get() = mainBinding.mainTabs

    val bread_crumbs: BreadCrumbLayout
        get() = mainBinding.mainBreadCrumbs
}
