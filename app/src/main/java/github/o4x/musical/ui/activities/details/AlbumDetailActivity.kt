package github.o4x.musical.ui.activities.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import github.o4x.musical.R
import github.o4x.musical.helper.MusicPlayerRemote.enqueue
import github.o4x.musical.helper.MusicPlayerRemote.openAndShuffleQueue
import github.o4x.musical.helper.MusicPlayerRemote.playNext
import github.o4x.musical.model.Album
import github.o4x.musical.model.Song
import github.o4x.musical.ui.dialogs.AddToPlaylistDialog
import github.o4x.musical.ui.dialogs.DeleteSongsDialog
import github.o4x.musical.ui.viewmodel.AlbumDetailsViewModel
import github.o4x.musical.util.NavigationUtil
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Be careful when changing things in this Activity!
 */
class AlbumDetailActivity : AbsDetailActivity<Album>() {

    private val detailsViewModel by viewModel<AlbumDetailsViewModel> {
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
        setAlbum(
            detailsViewModel.loadAlbumSync()
        )
        detailsViewModel.getAlbum().observe(this, {
            setAlbum(it)
        })
    }

    private fun setAlbum(album: Album) {
        this.data = album

        loadImage()

        songAdapter?.swapDataSet(album.songs)
        songAdapter?.data = album
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
        menuInflater.inflate(R.menu.menu_album_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val songs = getSongs()
        when (id) {
            R.id.action_shuffle_album -> {
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
            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(songs).show(supportFragmentManager, "DELETE_SONGS")
                return true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            R.id.action_tag_editor -> {
                NavigationUtil.goToAlbumTagEditor(this, data!!)
                return true
            }
            R.id.action_go_to_artist -> {
                NavigationUtil.goToArtist(this, data!!.artistId)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
