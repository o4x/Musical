package github.o4x.musical.ui.activities.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import github.o4x.musical.R
import github.o4x.musical.helper.MusicPlayerRemote.enqueue
import github.o4x.musical.helper.MusicPlayerRemote.openAndShuffleQueue
import github.o4x.musical.helper.MusicPlayerRemote.playNext
import github.o4x.musical.model.Artist
import github.o4x.musical.model.Song
import github.o4x.musical.ui.dialogs.AddToPlaylistDialog
import github.o4x.musical.ui.viewmodel.ArtistDetailsViewModel
import github.o4x.musical.util.NavigationUtil
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Be careful when changing things in this Activity!
 */
class ArtistDetailActivity : AbsDetailActivity<Artist>() {

    private val detailsViewModel: ArtistDetailsViewModel by viewModel {
        parametersOf(intent.extras!!.getLong(EXTRA_ID))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addMusicServiceEventListener(detailsViewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeMusicServiceEventListener(detailsViewModel)
    }

    override fun initObserver() {
        setArtist(
            detailsViewModel.loadArtistSync()
        )
        detailsViewModel.getArtist().observe(this, {
            setArtist(it)
        })
    }

    private fun setArtist(artist: Artist) {
        this.data = artist

        loadImage()

        songAdapter?.swapDataSet(artist.songs)
        songAdapter?.data = artist
    }

    override fun getSongs(): List<Song> {
        return data!!.songs
    }

    override fun loadImage() {
        getImageLoader()
            .load(data!!)
            .into(binding.image)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_artist_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val songs = getSongs()
        when (id) {
            R.id.action_shuffle_artist -> {
                openAndShuffleQueue(songs, true)
                return true
            }
            R.id.action_play_next -> {
                playNext(songs)
                return true
            }
            R.id.action_add_to_current_playing -> {
                enqueue(songs)
                return true
            }
            R.id.action_add_to_playlist -> {
                AddToPlaylistDialog.create(songs).show(supportFragmentManager, "ADD_PLAYLIST")
                return true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            R.id.action_tag_editor -> {
                NavigationUtil.goToArtistTagEditor(this, data!!)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
