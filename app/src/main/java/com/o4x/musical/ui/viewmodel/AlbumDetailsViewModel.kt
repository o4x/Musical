package com.o4x.musical.ui.viewmodel

import androidx.lifecycle.*
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.network.Result
import com.o4x.musical.network.Models.LastFmAlbum
import com.o4x.musical.repository.RealRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AlbumDetailsViewModel(
    private val repository: RealRepository,
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

    fun loadAlbumSync(): Album {
        return repository.albumRepository.album(albumId)
    }

    private fun fetchAlbum() {
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