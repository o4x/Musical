package com.o4x.musical.ui.activities.details

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote.enqueue
import com.o4x.musical.helper.MusicPlayerRemote.openAndShuffleQueue
import com.o4x.musical.helper.MusicPlayerRemote.playNext
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Song
import com.o4x.musical.network.Models.LastFmArtist
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity
import com.o4x.musical.ui.activities.tageditor.ArtistTagEditorActivity
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog
import com.o4x.musical.ui.dialogs.SleepTimerDialog
import com.o4x.musical.ui.viewmodel.ArtistDetailsViewModel
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.PreferenceUtil.isAllowedToDownloadMetadata
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*
import com.o4x.musical.network.Result
import com.o4x.musical.ui.adapter.song.DetailsSongAdapter
import kotlinx.android.synthetic.main.activity_detail.*

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

        toolbar.title = artist.name

        songAdapter?.swapDataSet(artist.songs)
        songAdapter?.data = artist
    }

    override fun getSongs(): List<Song> {
        return data!!.songs
    }

    override fun loadImage() {
        getImageLoader()
            .load(data!!)
            .into(image)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_artist_detail, menu)
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
                super.onBackPressed()
                return true
            }
            R.id.action_tag_editor -> {
                val editor = Intent(this, ArtistTagEditorActivity::class.java)
                editor.putExtra(AbsTagEditorActivity.EXTRA_ID, data!!.id)
                startActivityForResult(editor, TAG_EDITOR_REQUEST)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}