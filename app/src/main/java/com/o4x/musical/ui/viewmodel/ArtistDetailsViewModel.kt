package com.o4x.musical.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.o4x.musical.network.Result
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.model.Artist
import com.o4x.musical.network.Models.LastFmArtist
import com.o4x.musical.repository.RealRepository
import kotlinx.coroutines.Dispatchers.IO

class ArtistDetailsViewModel(
    private val realRepository: RealRepository,
    private val artistId: Long
) : ViewModel(), MusicServiceEventListener {

    fun getArtist(): LiveData<Artist> = liveData(IO) {
        val artist = realRepository.artistById(artistId)
        emit(artist)
    }

    fun getArtistInfo(
        name: String,
        lang: String?,
        cache: String?
    ): LiveData<Result<LastFmArtist>> = liveData(IO) {
        emit(Result.Loading)
        val info = realRepository.artistInfo(name, lang, cache)
        emit(info)
    }

    override fun onMediaStoreChanged() {
        getArtist()
    }

    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
}