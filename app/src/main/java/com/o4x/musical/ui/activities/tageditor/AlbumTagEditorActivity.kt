package com.o4x.musical.ui.activities.tageditor

import android.content.Intent
import android.os.Bundle
import com.o4x.musical.extensions.show
import com.o4x.musical.model.Artist
import com.o4x.musical.network.models.ITunesModel.Results
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity
import java.util.*

class AlbumTagEditorActivity : AbsTagEditorActivity<Results>() {

    override fun albumImageView() = binding.backImage
    override fun artistImageView() = binding.frontImage

    override fun onCreate(savedInstanceState: Bundle?) {
        album = repository.albumById(id)
        super.onCreate(savedInstanceState)
    }

    override fun showViews() {
        binding.apply {
            album.show()
            artist.show()
            year.show()
            genre.show()
        }
    }

    override fun fillViewsWithResult(result: Results) {
        loadImageFromUrl(result.bigArtworkUrl, null)
        binding.album.editText?.setText(result.collectionName)
        binding.artist.editText?.setText(result.artistName)
        binding.year.editText?.setText(result.year)
        binding.genre.editText?.setText(result.primaryGenreName)
    }

    override fun createArtist(): Artist =
        repository.artistById(album!!.artistId)

    override fun createPaths(): List<String> {
        val songs = album!!.songs
        val paths: MutableList<String> = ArrayList(songs.size)
        for (song in songs) {
            paths.add(song.data)
        }
        return paths
    }

    override fun searchImageOnWeb() {
        searchWebFor(binding.album.editText?.text.toString(),
            binding.artist.editText?.text.toString())
    }

    override fun searchOnline() {
        val intent = Intent(this, AlbumSearchActivity::class.java)
        intent.putExtra(AlbumSearchActivity.EXTRA_SONG_NAME, tagUtil?.albumTitle)
        this.startActivityForResult(intent, AlbumSearchActivity.REQUEST_CODE)
    }
}