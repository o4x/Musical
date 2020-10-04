package com.o4x.musical.ui.activities.tageditor

import android.content.Intent
import android.os.Bundle
import butterknife.ButterKnife
import com.o4x.musical.R
import com.o4x.musical.loader.ArtistLoader
import com.o4x.musical.model.Artist
import com.o4x.musical.network.Models.ITunesModel.Results
import com.o4x.musical.repository.RealAlbumRepository
import com.o4x.musical.repository.RealSongRepository
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity
import java.util.*

class AlbumTagEditorActivity : AbsTagEditorActivity<Results>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
    }

    override val contentViewLayout: Int
        get() = R.layout.activity_album_tag_editor

    override fun fillViewsWithResult(result: Results) {
        loadImageFromUrl(result.bigArtworkUrl, null)
        songName?.setText(result.trackName)
        albumName?.setText(result.collectionName)
        artistName?.setText(result.artistName)
        genreName?.setText(result.primaryGenreName)
        year?.setText(result.year)
        trackNumber?.setText(result.trackNumber.toString())
    }

    override val artist: Artist
        get() = ArtistLoader.getArtist(
            this,
            RealAlbumRepository(RealSongRepository(this)).album(id).artistId
        )

    override val songPaths: List<String>
        get() {
            val songs = RealAlbumRepository(RealSongRepository(this)).album(id).songs
            val paths: MutableList<String> = ArrayList(songs.size)
            for (song in songs) {
                paths.add(song.data)
            }
            return paths
        }

    override fun searchImageOnWeb() {
        searchWebFor(albumName?.text.toString(), artistName?.text.toString())
    }

    override fun searchOnline() {
        val intent = Intent(this, AlbumSearchActivity::class.java)
        intent.putExtra(AlbumSearchActivity.EXTRA_SONG_NAME, tagUtil?.albumTitle)
        this.startActivityForResult(intent, AlbumSearchActivity.REQUEST_CODE)
    }
}