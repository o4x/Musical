package github.o4x.musical.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.o4x.musical.interfaces.MusicServiceEventListener
import github.o4x.musical.model.Playlist
import github.o4x.musical.model.Song
import github.o4x.musical.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistDetailsViewModel(
    private val realRepository: Repository,
    private var playlist: Playlist
) : ViewModel(), MusicServiceEventListener {

    val playListSongs = MutableLiveData<List<Song>>()

    init {
        loadSongs(playlist)
    }

    private fun loadSongs(playlist: Playlist) = viewModelScope.launch {
        val songs = realRepository.getPlaylistSongs(playlist)
        withContext(Dispatchers.Main) { playListSongs.postValue(songs) }
    }

    override fun onMediaStoreChanged() {
        loadSongs(playlist)
    }
    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
}
