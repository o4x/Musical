package github.o4x.m2.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import github.o4x.m2.App
import github.o4x.m2.db.*
import github.o4x.m2.helper.MusicPlayerRemote
import github.o4x.m2.interfaces.MusicServiceEventListener
import github.o4x.m2.model.*
import github.o4x.m2.prefs.PreferenceUtil
import github.o4x.m2.repository.Repository
import github.o4x.m2.shared.Permissions
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            // Heavy library queries start only after home content is already visible.
            // Songs stream in first so the visible list paints immediately; the derived
            // album/artist/genre queries wait instead of competing for the ContentProvider.
            fetchSongs().join()
            fetchAlbums()
            fetchArtists()
            fetchGenres()
            fetchLegacyPlaylist()
        } else {
            _isLoading.postValue(false)
        }
    }

    // Streaming loads post many small snapshots; keep one job per loader so a
    // reload cancels the stream still in flight instead of interleaving with it.
    private var songsJob: Job? = null
    private var albumsJob: Job? = null
    private var artistsJob: Job? = null
    private var genresJob: Job? = null

    private fun fetchSongs(): Job {
        songsJob?.cancel()
        return viewModelScope.launch(IO) {
            _isLoading.postValue(true)
            repository.allSongsFlow().collect { chunk ->
                songs.postValue(chunk)
                _isLoading.postValue(false)
            }
        }.also { songsJob = it }
    }

    private fun fetchAlbums() {
        albumsJob?.cancel()
        albumsJob = viewModelScope.launch(IO) {
            repository.albumsFlow().collect { albums.postValue(it) }
        }
    }

    private fun fetchArtists() {
        artistsJob?.cancel()
        artistsJob = viewModelScope.launch(IO) {
            val flow = if (PreferenceUtil.albumArtistsOnly) {
                repository.albumArtistsFlow()
            } else {
                repository.artistsFlow()
            }
            flow.collect { artists.postValue(it) }
        }
    }

    private fun fetchLegacyPlaylist() {
        viewModelScope.launch(IO) {
            legacyPlaylists.postValue(repository.fetchLegacyPlaylist())
        }
    }

    private fun fetchGenres() {
        genresJob?.cancel()
        genresJob = viewModelScope.launch(IO) {
            repository.genresFlow().collect { genres.postValue(it) }
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
