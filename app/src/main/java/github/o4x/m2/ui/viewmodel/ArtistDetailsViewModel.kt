package github.o4x.m2.ui.viewmodel

import androidx.lifecycle.*
import github.o4x.m2.interfaces.MusicServiceEventListener
import github.o4x.m2.model.Artist
import github.o4x.m2.network.models.LastFmArtist
import github.o4x.m2.network.Result
import github.o4x.m2.repository.Repository
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

    init {
        fetchArtist()
    }

    fun fetchArtist() {
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