package com.o4x.musical.ui.viewmodel

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.*
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.App
import com.o4x.musical.db.*
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.imageloader.glide.targets.palette.PaletteTargetListener
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.model.*
import com.o4x.musical.repository.RealRepository
import com.o4x.musical.util.CoverUtil
import com.o4x.musical.helper.MyPalette
import com.o4x.musical.imageloader.glide.targets.CustomBitmapTarget
import com.o4x.musical.prefs.PreferenceUtil
import com.o4x.musical.prefs.PreferenceUtil.isDarkMode
import com.o4x.musical.util.Util
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: RealRepository
) : ViewModel(), MusicServiceEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val paletteColor = MutableLiveData<Int>()

    private val albums = MutableLiveData<List<Album>>()
    private val songs = MutableLiveData<List<Song>>()
    private val artists = MutableLiveData<List<Artist>>()
    private val playlists = MutableLiveData<List<PlaylistWithSongs>>()
    private val legacyPlaylists = MutableLiveData<List<Playlist>>()
    private val genres = MutableLiveData<List<Genre>>()
    private val searchResults = MutableLiveData<List<Any>>()
    private val recentlyAdded = MutableLiveData<List<Song>>()
    private val recentlyPlayed = MutableLiveData<List<Song>>()

    fun getPaletteColor(): LiveData<Int> = paletteColor
    fun getSearchResult(): LiveData<List<Any>> = searchResults
    fun getSongs(): LiveData<List<Song>> = songs
    fun getAlbums(): LiveData<List<Album>> = albums
    fun getArtists(): LiveData<List<Artist>> = artists
    fun getPlaylists(): LiveData<List<PlaylistWithSongs>> = playlists
    fun getLegacyPlaylist(): LiveData<List<Playlist>> = legacyPlaylists
    fun getGenre(): LiveData<List<Genre>> = genres
    fun getRecentlyPlayed(): LiveData<List<Song>> = recentlyPlayed
    fun getRecentlyAdded(): LiveData<List<Song>> = recentlyAdded

    init {
        loadLibraryContent()
    }

    private fun loadLibraryContent() = viewModelScope.launch(IO) {
        fetchSongs()
        fetchAlbums()
        fetchArtists()
        fetchGenres()
        fetchPlaylists()
        fetchLegacyPlaylist()
        fetchRecentlyPlayed()
        fetchRecentlyAdded()
    }

    private fun fetchSongs() {
        viewModelScope.launch(IO) {
            songs.postValue(repository.allSongs())
        }
    }

    private fun fetchAlbums() {
        viewModelScope.launch(IO) {
            albums.postValue(repository.fetchAlbums())
        }
    }

    private fun fetchArtists() {
        if (PreferenceUtil.albumArtistsOnly) {
            viewModelScope.launch(IO) {
                artists.postValue(repository.albumArtists())
            }
        } else {
            viewModelScope.launch(IO) {
                artists.postValue(repository.fetchArtists())
            }
        }
    }

    private fun fetchPlaylists() {
        viewModelScope.launch(IO) {
            playlists.postValue(repository.fetchPlaylistWithSongs())
        }
    }

    private fun fetchLegacyPlaylist() {
        viewModelScope.launch(IO) {
            legacyPlaylists.postValue(repository.fetchLegacyPlaylist())
        }
    }

    private fun fetchGenres() {
        viewModelScope.launch(IO) {
            genres.postValue(repository.fetchGenres())
        }
    }

    private fun fetchRecentlyPlayed() {
        viewModelScope.launch(IO) {
            recentlyPlayed.postValue(repository.topPlayedRepository.recentlyPlayedTracks())
        }
    }

    private fun fetchRecentlyAdded() {
        viewModelScope.launch(IO) {
            recentlyAdded.postValue(repository.recentSongs())
        }
    }

    fun forceReload(reloadType: ReloadType) = viewModelScope.launch {
        when (reloadType) {
            ReloadType.Songs -> fetchSongs()
            ReloadType.Albums -> fetchAlbums()
            ReloadType.Artists -> fetchArtists()
            ReloadType.Playlists -> {
                fetchPlaylists()
                fetchLegacyPlaylist()
                fetchRecentlyPlayed()
                fetchRecentlyAdded()
            }
            ReloadType.Genres -> fetchGenres()
        }
    }

    fun updateColor(newColor: Int) {
        paletteColor.postValue(newColor)
    }

    override fun onMediaStoreChanged() {
        println("onMediaStoreChanged")
        loadLibraryContent()
    }

    override fun onServiceConnected() {
        println("onServiceConnected")
    }

    override fun onServiceDisconnected() {
        println("onServiceDisconnected")
    }

    override fun onQueueChanged() {
        println("onQueueChanged")
    }

    override fun onPlayingMetaChanged() {
        println("onPlayingMetaChanged")
        fetchRecentlyPlayed()
    }

    override fun onPlayStateChanged() {
        println("onPlayStateChanged")
    }

    override fun onRepeatModeChanged() {
        println("onRepeatModeChanged")
    }

    override fun onShuffleModeChanged() {
        println("onShuffleModeChanged")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.SMART_PLAYLIST_LIMIT -> forceReload(ReloadType.Playlists)
        }
    }

    fun search(query: String?) = viewModelScope.launch(IO) {
        val result = repository.search(query)
        searchResults.postValue(result)
    }

    fun shuffleSongs() = viewModelScope.launch(IO) {
        val songs = repository.allSongs()
        MusicPlayerRemote.openAndShuffleQueue(
            songs,
            true
        )
    }

    fun renameRoomPlaylist(playListId: Long, name: String) = viewModelScope.launch(IO) {
        repository.renameRoomPlaylist(playListId, name)
    }

    fun deleteSongsInPlaylist(songs: List<SongEntity>) = viewModelScope.launch(IO) {
        repository.deleteSongsInPlaylist(songs)
    }

    fun deleteSongsFromPlaylist(playlists: List<PlaylistEntity>) = viewModelScope.launch(IO) {
        repository.deletePlaylistSongs(playlists)
    }

    fun deleteRoomPlaylist(playlists: List<PlaylistEntity>) = viewModelScope.launch(IO) {
        repository.deleteRoomPlaylist(playlists)
    }

    suspend fun albumById(id: Long) = repository.albumById(id)
    suspend fun artistById(id: Long) = repository.artistByIdAsync(id)
    suspend fun insertSongs(songs: List<SongEntity>) = repository.insertSongs(songs)
    suspend fun removeSongFromPlaylist(songEntity: SongEntity) =
        repository.removeSongFromPlaylist(songEntity)

    suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity> =
        repository.checkPlaylistExists(playlistName)

    suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long =
        repository.createPlaylist(playlistEntity)

    fun importPlaylists() = viewModelScope.launch(IO) {
        val playlists = repository.fetchLegacyPlaylist()
        playlists.forEach { playlist ->
            val playlistEntity = repository.checkPlaylistExists(playlist.name).firstOrNull()
            if (playlistEntity != null) {
                val songEntities = playlist.songs().map {
                    it.toSongEntity(playlistEntity.playListId)
                }
                repository.insertSongs(songEntities)
            } else {
                val playListId = createPlaylist(PlaylistEntity(playlistName = playlist.name))
                val songEntities = playlist.songs().map {
                    it.toSongEntity(playListId)
                }
                repository.insertSongs(songEntities)
            }
            forceReload(ReloadType.Playlists)
        }
    }

    fun deleteTracks(songs: List<Song>) = viewModelScope.launch(IO) {
        repository.deleteSongs(songs)
    }

    fun recentSongs(): LiveData<List<Song>> = liveData {
        emit(repository.recentSongs())
    }

    fun playCountSongs(): LiveData<List<Song>> = liveData {
        emit(repository.playCountSongs().map {
            it.toSong()
        })
    }

    fun artist(artistId: Long): LiveData<Artist> = liveData {
        emit(repository.artistByIdAsync(artistId))
    }

    fun playlist(playListId: Long): LiveData<Playlist> = liveData {
        emit(repository.playlist(playListId))
    }

    fun observableHistorySongs() = repository.observableHistorySongs()

    fun clearSearchResult() {
        viewModelScope.launch {
            searchResults.postValue(emptyList())
        }
    }
}

enum class ReloadType {
    Songs,
    Albums,
    Artists,
    Playlists,
    Genres,
}