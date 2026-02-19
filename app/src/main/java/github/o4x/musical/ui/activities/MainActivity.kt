package github.o4x.musical.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import github.o4x.musical.R
import github.o4x.musical.databinding.ActivityMainBinding
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
    }

    private lateinit var binding: ActivityMainBinding

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDrawUnderStatusBar()
        setNavigationBarColorAuto()
        setStatusBarColorAuto()
        setLightNavigationBar(true)

        setupNavController()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun createContentView(): View {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return wrapSlidingMusicPanel(binding.root)
    }

    fun openSearch() {
        navController.navigate(R.id.action_to_search)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handlePlaybackIntent(intent)
    }

    override fun handleBackPress(): Boolean {
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
    }
}
