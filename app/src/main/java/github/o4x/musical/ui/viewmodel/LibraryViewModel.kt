package github.o4x.musical.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import github.o4x.musical.App
import github.o4x.musical.db.*
import github.o4x.musical.helper.MusicPlayerRemote
import github.o4x.musical.interfaces.MusicServiceEventListener
import github.o4x.musical.model.*
import github.o4x.musical.prefs.PreferenceUtil
import github.o4x.musical.repository.Repository
import github.o4x.musical.shared.Permissions
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(
    private val repository: Repository
) : ViewModel(), MusicServiceEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val paletteColor = MutableLiveData<Int>()

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val albums = MutableLiveData<List<Album>>()
    private val songs = MutableLiveData<List<Song>>()
    private val artists = MutableLiveData<List<Artist>>()
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
    fun getLegacyPlaylist(): LiveData<List<Playlist>> = legacyPlaylists
    fun getGenre(): LiveData<List<Genre>> = genres
    fun getRecentlyPlayed(): LiveData<List<Song>> = recentlyPlayed
    fun getRecentlyAdded(): LiveData<List<Song>> = recentlyAdded

    init {
        loadLibraryContent()
    }

    private fun loadLibraryContent() = viewModelScope.launch(IO) {
        if (Permissions.canReadStorage(App.getContext())) {
            // Home page data first: two small queries in parallel, no competition with heavy loads
            coroutineScope {
                launch { recentlyPlayed.postValue(repository.historySong()) }
                launch { recentlyAdded.postValue(repository.recentSongs()) }
            }
            // Heavy library queries start only after home content is already visible
            fetchSongs()
            fetchAlbums()
            fetchArtists()
            fetchGenres()
            fetchLegacyPlaylist()
        } else {
            _isLoading.postValue(false)
        }
    }

    private fun fetchSongs() {
        viewModelScope.launch(IO) {
            _isLoading.postValue(true)
            val result = repository.allSongs()
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                songs.value = result
                _isLoading.value = false
            }
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
            recentlyPlayed.postValue(repository.historySong())
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
        loadLibraryContent()
    }

    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}

    override fun onPlayingMetaChanged() {
        fetchRecentlyPlayed()
    }

    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceUtil.SMART_PLAYLIST_LIMIT -> forceReload(ReloadType.Playlists)
        }
    }

    private var searchJob: Job? = null

    fun search(realContext: Context, query: String?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(IO) {
            delay(300)
            val result = repository.search(realContext, query)
            searchResults.postValue(result)
        }
    }

    fun shuffleSongs() = viewModelScope.launch(IO) {
        val songs = repository.allSongs()
        MusicPlayerRemote.openAndShuffleQueue(
            songs,
            true
        )
    }

    suspend fun albumById(id: Long) = repository.albumById(id)
    suspend fun artistById(id: Long) = repository.artistByIdAsync(id)

    // Critical Performance Fix: Added IO dispatcher to prevent Main Thread blocking
    fun recentSongs(): LiveData<List<Song>> = liveData(IO) {
        emit(repository.recentSongs())
    }

    fun playCountSongs(): LiveData<List<Song>> = liveData(IO) {
        emit(repository.playCountSongs())
    }

    fun artist(artistId: Long): LiveData<Artist> = liveData(IO) {
        emit(repository.artistByIdAsync(artistId))
    }

    fun playlist(playListId: Long): LiveData<Playlist> = liveData(IO) {
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
    Songs, Albums, Artists, Playlists, Genres,
}
