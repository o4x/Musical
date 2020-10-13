package com.o4x.musical.ui.activities.details

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote.enqueue
import com.o4x.musical.helper.MusicPlayerRemote.openAndShuffleQueue
import com.o4x.musical.helper.MusicPlayerRemote.playNext
import com.o4x.musical.model.Album
import com.o4x.musical.model.Song
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity
import com.o4x.musical.ui.activities.tageditor.AlbumTagEditorActivity
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog
import com.o4x.musical.ui.dialogs.DeleteSongsDialog
import com.o4x.musical.ui.dialogs.SleepTimerDialog
import com.o4x.musical.ui.viewmodel.AlbumDetailsViewModel
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.PreferenceUtil.isAllowedToDownloadMetadata
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

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
        val isFirst = this.data == null
        this.data = album

        if (isFirst) {
            loadImageSync()
        } else {
            loadImage()
        }

//        artistTextView.setText(album.getArtistName());
//        songCountTextView.setText(MusicUtil.getSongCountString(this, album.getSongCount()));
//        durationTextView.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(this, album.getSongs())));
//        albumYearTextView.setText(MusicUtil.getYearString(album.getYear()));
        toolbar.title = album.title
        songAdapter?.swapDataSet(album.songs)
        songAdapter?.data = album
    }

    override fun getSongs(): List<Song> {
        return data!!.songs
    }

    override fun loadImage() {
        imageLoader.byThis(data!!).displayInTo(image)
    }

    override fun loadImageSync() {
        imageLoader.byThis(data!!).loadImageSync(image)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_album_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val songs = getSongs()
        when (id) {
            R.id.action_sleep_timer -> {
                SleepTimerDialog().show(supportFragmentManager, "SET_SLEEP_TIMER")
                return true
            }
            R.id.action_equalizer -> {
                NavigationUtil.openEqualizer(this)
                return true
            }
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
                super.onBackPressed()
                return true
            }
            R.id.action_tag_editor -> {
                val intent = Intent(this, AlbumTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, data!!.id)
                startActivityForResult(intent, TAG_EDITOR_REQUEST)
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