package github.o4x.m2.ui.viewmodel

import androidx.lifecycle.*
import github.o4x.m2.interfaces.MusicServiceEventListener
import github.o4x.m2.model.Album
import github.o4x.m2.model.Artist
import github.o4x.m2.network.Result
import github.o4x.m2.network.models.LastFmAlbum
import github.o4x.m2.repository.Repository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AlbumDetailsViewModel(
    private val repository: Repository,
    private val albumId: Long
) : ViewModel(), MusicServiceEventListener {

    private val album: MutableLiveData<Album> by lazy {
        MutableLiveData<Album>()
    }

    fun getAlbum(): LiveData<Album> {
        return album
    }

    fun getArtist(artistId: Long): LiveData<Artist> = liveData(IO) {
        val artist = repository.artistByIdAsync(artistId)
        emit(artist)
    }

    fun getAlbumInfo(album: Album): LiveData<Result<LastFmAlbum>> = liveData {
        emit(Result.Loading)
        emit(repository.albumInfo(album.artistName ?: "-", album.title ?: "-"))
    }

    fun getMoreAlbums(artist: Artist): LiveData<List<Album>> = liveData(IO) {
        artist.albums.filter { item -> item.id != albumId }.let { albums ->
            if (albums.isNotEmpty()) emit(albums)
        }
    }

    init {
        fetchAlbum()
    }

    fun fetchAlbum() {
        viewModelScope.launch(IO) {
            album.postValue(
                repository.albumById(albumId)
            )
        }
    }

    override fun onMediaStoreChanged() {
        fetchAlbum()
    }

    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
}