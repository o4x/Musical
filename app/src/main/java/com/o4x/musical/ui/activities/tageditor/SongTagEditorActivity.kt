package com.o4x.musical.ui.activities.tageditor

import android.content.Intent
import android.os.Bundle
import com.o4x.musical.extensions.show
import com.o4x.musical.model.Artist
import com.o4x.musical.network.models.ITunesModel.Results
import com.o4x.musical.ui.activities.tageditor.onlinesearch.SongSearchActivity
import java.util.*

class SongTagEditorActivity : AbsTagEditorActivity<Results>() {

    override fun albumImageView() = binding.backImage
    override fun artistImageView() = binding.frontImage

    val song by lazy { repository.songById(id) }

    override fun onCreate(savedInstanceState: Bundle?) {
        album = repository.albumById(song.albumId)
        super.onCreate(savedInstanceState)
    }

    override fun showViews() {
        binding.apply {
            song.show()
            album.show()
            artist.show()
            year.show()
            genre.show()
            track.show()
            disc.show()
            lyrics.show()
        }
    }

    override fun createArtist(): Artist =
        repository.artistById(song.artistId)

    override fun createPaths(): List<String> {
        val paths: MutableList<String> = ArrayList(1)
        paths.add(song.data)
        return paths
    }

    override fun fillViewsWithResult(result: Results) {
        loadImageFromUrl(result.bigArtworkUrl, null)
        binding.song.editText?.setText(result.trackName)
        binding.album.editText?.setText(result.collectionName)
        binding.artist.editText?.setText(result.artistName)
        binding.genre.editText?.setText(result.primaryGenreName)
        binding.year.editText?.setText(result.year)
        binding.track.editText?.setText(result.trackNumber.toString())
    }

    override fun searchImageOnWeb() {
        searchWebFor(
            binding.song.editText?.text.toString(),
            binding.album.editText?.text.toString(),
            binding.artist.editText?.text.toString()
        )
    }

    override fun searchOnline() {
        val intent = Intent(this, SongSearchActivity::class.java)
        intent.putExtra(SongSearchActivity.EXTRA_SONG_NAME, tagUtil?.songTitle)
        this.startActivityForResult(intent, SongSearchActivity.REQUEST_CODE)
    }
}