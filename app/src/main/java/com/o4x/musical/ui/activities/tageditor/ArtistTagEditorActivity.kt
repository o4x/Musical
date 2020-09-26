package com.o4x.musical.ui.activities.tageditor

import android.content.Intent
import com.o4x.musical.R
import com.o4x.musical.loader.ArtistLoader
import com.o4x.musical.model.Artist
import com.o4x.musical.network.Models.DeezerArtistModel
import com.o4x.musical.ui.activities.tageditor.onlinesearch.ArtistSearchActivity
import java.util.*

class ArtistTagEditorActivity : AbsTagEditorActivity<DeezerArtistModel.Data>() {
    override val contentViewLayout: Int
        get() = R.layout.activity_artist_tag_editor

    override val artist: Artist
        get() = ArtistLoader.getArtist(
            this,
            id
        )

    override val songPaths: List<String>
        get() {
            val songs = ArtistLoader.getArtist(this, id).songs
            val paths: MutableList<String> = ArrayList(songs.size)
            for (song in songs) {
                paths.add(song.data)
            }
            return paths
        }

    override fun fillViewsWithResult(result: DeezerArtistModel.Data) {
        loadImageFromUrl(result.pictureXl, null)
        artistName?.setText(result.name)
    }

    override fun searchImageOnWeb() {
        searchWebFor(artistName?.text.toString())
    }

    override fun searchOnline() {
        val intent = Intent(this, ArtistSearchActivity::class.java)
        intent.putExtra(ArtistSearchActivity.EXTRA_SONG_NAME, tagUtil!!.artistName)
        this.startActivityForResult(intent, ArtistSearchActivity.REQUEST_CODE)
    }
}