package com.o4x.musical.ui.viewmodel

import androidx.lifecycle.*
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.model.Artist
import com.o4x.musical.network.models.LastFmArtist
import com.o4x.musical.network.Result
import com.o4x.musical.repository.Repository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ArtistDetailsViewModel(
    private val realRepository: Repository,
    private val artistId: Long
) : ViewModel(), MusicServiceEventListener {

    private val artist: MutableLiveData<Artist> by lazy {
        MutableLiveData<Artist>()
    }

    fun getArtist(): LiveData<Artist> {
        return artist
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

    fun loadArtistSync(): Artist {
        return realRepository.artistRepository.artist(artistId)
    }

    private fun fetchArtist() {
        viewModelScope.launch(IO) {
            artist.postValue(
                realRepository.artistByIdAsync(artistId)
            )
        }
    }

    override fun onMediaStoreChanged() {
        fetchArtist()
    }

    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
}