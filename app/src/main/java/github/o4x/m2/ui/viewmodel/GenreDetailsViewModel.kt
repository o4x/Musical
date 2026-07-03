package github.o4x.m2.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.o4x.m2.interfaces.MusicServiceEventListener
import github.o4x.m2.model.Genre
import github.o4x.m2.model.Song
import github.o4x.m2.repository.Repository
import kotlinx.coroutines.launch

class GenreDetailsViewModel(
    private val realRepository: Repository,
    private val genre: Genre
) : ViewModel(), MusicServiceEventListener {

    private val _playListSongs = MutableLiveData<List<Song>>()
    private val _genre = MutableLiveData<Genre>().apply {
        postValue(genre)
    }

    fun getSongs(): LiveData<List<Song>> = _playListSongs

    fun getGenre(): LiveData<Genre> = _genre

    init {
        loadGenreSongs(genre)
    }

    private fun loadGenreSongs(genre: Genre) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val songs = realRepository.getGenre(genre.id)
        _playListSongs.postValue(songs)
    }

    override fun onMediaStoreChanged() {
        loadGenreSongs(genre)
    }

    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
}
