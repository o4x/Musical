/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.o4x.musical.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.o4x.musical.R
import com.o4x.musical.db.*
import com.o4x.musical.model.*
import com.o4x.musical.network.Models.LastFmAlbum
import com.o4x.musical.network.Models.LastFmArtist
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
    val searchRepository: RealSearchRepository,
    val topPlayedRepository: TopPlayedRepository,
    val roomRepository: RoomRepository,
) {


    suspend fun deleteSongs(songs: List<Song>) = roomRepository.deleteSongs(songs)

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

    suspend fun topArtists(): List<Artist> = topPlayedRepository.topArtists()

    suspend fun topAlbums(): List<Album> = topPlayedRepository.topAlbums()

    suspend fun fetchLegacyPlaylist(): List<Playlist> = playlistRepository.playlists()

    suspend fun fetchGenres(): List<Genre> = genreRepository.genres()

    suspend fun allSongs(): List<Song> = songRepository.songs()

    suspend fun search(query: String?): MutableList<Any> =
        searchRepository.searchAll(context, query)

    suspend fun getPlaylistSongs(playlist: Playlist): List<Song> =
        if (playlist is AbsCustomPlaylist) {
            playlist.songs()
        } else {
            PlaylistSongsLoader.getPlaylistSongList(context, playlist.id)
        }

    suspend fun getGenre(genreId: Long): List<Song> = genreRepository.songs(genreId)

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

    suspend fun playlist(playlistId: Long) =
        playlistRepository.playlist(playlistId)

    suspend fun addSongToHistory(currentSong: Song) =
        roomRepository.addSongToHistory(currentSong)

    suspend fun songPresentInHistory(currentSong: Song): HistoryEntity? =
        roomRepository.songPresentInHistory(currentSong)

    suspend fun updateHistorySong(currentSong: Song) =
        roomRepository.updateHistorySong(currentSong)

    suspend fun recentSongs(): List<Song> = lastAddedRepository.recentSongs()

    suspend fun topPlayedSongs(): List<Song> = topPlayedRepository.topTracks()

    suspend fun insertSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.insertSongInPlayCount(playCountEntity)

    suspend fun updateSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.updateSongInPlayCount(playCountEntity)

    suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity) =
        roomRepository.deleteSongInPlayCount(playCountEntity)

    suspend fun checkSongExistInPlayCount(songId: Long): List<PlayCountEntity> =
        roomRepository.checkSongExistInPlayCount(songId)

    suspend fun playCountSongs(): List<PlayCountEntity> =
        roomRepository.playCountSongs()

    fun observableHistorySongs(): LiveData<List<Song>> =
        Transformations.map(roomRepository.observableHistorySongs()) {
            it.fromHistoryToSongs()
        }

    fun historySong(): List<HistoryEntity> =
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