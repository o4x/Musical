package com.o4x.musical.ui.activities.tageditor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import com.o4x.musical.extensions.hide
import com.o4x.musical.extensions.show
import com.o4x.musical.model.Artist
import com.o4x.musical.network.Models.DeezerArtistModel
import com.o4x.musical.ui.activities.tageditor.onlinesearch.ArtistSearchActivity
import java.util.*

class ArtistTagEditorActivity : AbsTagEditorActivity<DeezerArtistModel.Data>() {

    override fun albumImageView() = binding.frontImage
    override fun artistImageView() = binding.backImage

    override fun showViews() {
        binding.apply {
            artist.show()
            frontImage.hide()
        }
    }

    override fun createArtist(): Artist = repository.artistById(id)

    override fun createPaths(): List<String> {
        val songs = artist.songs
        val paths: MutableList<String> = ArrayList(songs.size)
        for (song in songs) {
            paths.add(song.data)
        }
        return paths
    }

    override fun fillViewsWithResult(result: DeezerArtistModel.Data) {
        loadImageFromUrl(result.pictureXl, null)
        binding.artist.editText?.setText(result.name)
    }

    override fun searchImageOnWeb() {
        searchWebFor(binding.artist.editText?.text.toString())
    }

    override fun searchOnline() {
        val intent = Intent(this, ArtistSearchActivity::class.java)
        intent.putExtra(ArtistSearchActivity.EXTRA_SONG_NAME, tagUtil!!.artistName)
        this.startActivityForResult(intent, ArtistSearchActivity.REQUEST_CODE)
    }
}