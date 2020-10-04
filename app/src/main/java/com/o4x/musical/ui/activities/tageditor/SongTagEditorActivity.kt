package com.o4x.musical.ui.activities.tageditor

import android.content.Intent
import com.o4x.musical.R
import com.o4x.musical.model.Artist
import com.o4x.musical.network.Models.ITunesModel.Results
import com.o4x.musical.repository.RealAlbumRepository
import com.o4x.musical.repository.RealArtistRepository
import com.o4x.musical.repository.RealSongRepository
import com.o4x.musical.ui.activities.tageditor.onlinesearch.SongSearchActivity
import java.util.*

class SongTagEditorActivity : AbsTagEditorActivity<Results>() {

    override val contentViewLayout: Int
        get() = R.layout.activity_song_tag_editor

    override val artist: Artist
        get() = RealArtistRepository(
            RealSongRepository(this),
            RealAlbumRepository(RealSongRepository(this))
        ).artist(
            RealSongRepository(this).song(id).artistId
        )

    override val songPaths: List<String>
        get() {
            val paths: MutableList<String> = ArrayList(1)
            paths.add(RealSongRepository(this).song(id).data)
            return paths
        }

    override fun fillViewsWithResult(result: Results) {
        loadImageFromUrl(result.bigArtworkUrl, null)
        songName?.setText(result.trackName)
        albumName?.setText(result.collectionName)
        artistName?.setText(result.artistName)
        genreName?.setText(result.primaryGenreName)
        year?.setText(result.year)
        trackNumber?.setText(result.trackNumber.toString())
    }

    override fun searchImageOnWeb() {
        searchWebFor(
            songName?.text.toString(),
            albumName?.text.toString(),
            artistName?.text.toString()
        )
    }

    override fun searchOnline() {
        val intent = Intent(this, SongSearchActivity::class.java)
        intent.putExtra(SongSearchActivity.EXTRA_SONG_NAME, tagUtil?.songTitle)
        this.startActivityForResult(intent, SongSearchActivity.REQUEST_CODE)
    }
}