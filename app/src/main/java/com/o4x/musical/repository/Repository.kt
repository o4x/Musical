package com.o4x.musical.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.o4x.musical.model.*
import com.o4x.musical.network.models.LastFmAlbum
import com.o4x.musical.network.models.LastFmArtist
import com.o4x.musical.network.Result
import com.o4x.musical.network.Result.*
import com.o4x.musical.network.service.LastFMService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository(
    val context: Context,
    val lastFMService: LastFMService,
    val songRepository: SongRepository,
    val albumRepository: AlbumRepository,
    val artistRepository: ArtistRepository,
    val genreRepository: GenreRepository,
    val lastAddedRepository: LastAddedRepository,
    val playlistRepository: PlaylistRepository,
    val searchRepository: SearchRepository,
    val roomRepository: RoomRepository,
) {

    suspend fun searchSongs(query: String): List<Song> = songRepository.songs(query)

    suspend fun searchAlbums(query: String): List<Album> = albumRepository.albums(query)

    suspend fun searchArtists(query: String): List<Artist> =
        artistRepository.artists(query)

    suspend fun fetchAlbums(): List<Album> = albumRepository.albums()

    suspend fun albumByIdAsync(albumId: Long): Album = albumRepository.album(albumId)

    fun songById(songId: Long): Song = songRepository.song(songId)

    fun albumById(albumId: Long): Album = albumRepository.album(albumId)

    fun artistById(artistId: Long): Artist = artistRepository.artist(artistId)

    suspend fun fetchArtists(): List<Artist> = artistRepository.artists()

    suspend fun albumArtists(): List<Artist> = artistRepository.albumArtists()

    suspend fun artistByIdAsync(artistId: Long): Artist = artistRepository.artist(artistId)

    suspend fun recentArtists(): List<Artist> = lastAddedRepository.recentArtists()

    suspend fun recentAlbums(): List<Album> = lastAddedRepository.recentAlbums()

    suspend fun topArtists(): List<Artist> = artistRepository.splitIntoArtists(topAlbums())

    suspend fun topAlbums(): List<Album> = albumRepository.splitIntoAlbums(topPlayedSongs())

    suspend fun fetchLegacyPlaylist(): List<Playlist> = playlistRepository.playlists()

    suspend fun fetchGenres(): List<Genre> = genreRepository.genres()

    suspend fun allSongs(): List<Song> = songRepository.songs()

    suspend fun search(realContext: Context, query: String?): MutableList<Any> =
        searchRepository.searchAll(realContext, query)

    fun getPlaylistSongs(playlist: Playlist): List<Song> =
        if (playlist is AbsCustomPlaylist) {
            playlist.songs()
        } else {
            PlaylistSongsLoader.getPlaylistSongList(context, playlist.id)
        }

    fun getGenre(genreId: Long): List<Song> = genreRepository.songs(genreId)

    suspend fun artistInfo(
        name: String,
        lang: String?,
        cache: String?
    ): Result<LastFmArtist> {
        return try {
            Success(lastFMService.artistInfo(name, lang, cache))
        } catch (e: Exception) {
            println(e)
            Error(e)
        }
    }

    suspend fun albumInfo(
        artist: String,
        album: String
    ): Result<LastFmAlbum> {
        return try {
            val lastFmAlbum = lastFMService.albumInfo(artist, album)
            Success(lastFmAlbum)
        } catch (e: Exception) {
            println(e)
            Error(e)
        }
    }

    fun playlist(playlistId: Long) =
        playlistRepository.playlist(playlistId)

    fun recentSongs(): List<Song> = lastAddedRepository.recentSongs()

    fun topPlayedSongs(): List<Song> = roomRepository.playCountSongs()

    fun playCountSongs(): List<Song> =
        roomRepository.playCountSongs()

    fun observableHistorySongs(): LiveData<List<Song>> =
        roomRepository.observableHistorySongs()

    fun historySong(): List<Song> =
        roomRepository.historySongs()

    fun songsFlow(): Flow<Result<List<Song>>> = flow {
        emit(Loading)
        val data = songRepository.songs()
        if (data.isEmpty()) {
            emit(Error(Exception(Throwable("No items"))))
        } else {
            emit(Success(data))
        }
    }

    fun albumsFlow(): Flow<Result<List<Album>>> = flow {
        emit(Loading)
        val data = albumRepository.albums()
        if (data.isEmpty()) {
            emit(Error(Exception(Throwable("No items"))))
        } else {
            emit(Success(data))
        }
    }

    fun artistsFlow(): Flow<Result<List<Artist>>> = flow {
        emit(Loading)
        val data = artistRepository.artists()
        if (data.isEmpty()) {
            emit(Error(Exception(Throwable("No items"))))
        } else {
            emit(Success(data))
        }
    }

    fun playlistsFlow(): Flow<Result<List<Playlist>>> = flow {
        emit(Loading)
        val data = playlistRepository.playlists()
        if (data.isEmpty()) {
            emit(Error(Exception(Throwable("No items"))))
        } else {
            emit(Success(data))
        }
    }

    fun genresFlow(): Flow<Result<List<Genre>>> = flow {
        emit(Loading)
        val data = genreRepository.genres()
        if (data.isEmpty()) {
            emit(Error(Exception(Throwable("No items"))))
        } else {
            emit(Success(data))
        }
    }
}