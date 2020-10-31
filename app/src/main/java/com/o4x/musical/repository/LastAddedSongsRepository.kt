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

import android.database.Cursor
import android.provider.MediaStore
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Song
import com.o4x.musical.util.PreferenceUtil.smartPlaylistLimit

/**
 * Created by hemanths on 16/08/17.
 */
interface LastAddedRepository {
    fun recentSongs(): List<Song>

    fun recentAlbums(): List<Album>

    fun recentArtists(): List<Artist>
}

class RealLastAddedRepository(
    private val songRepository: RealSongRepository,
    private val albumRepository: RealAlbumRepository,
    private val artistRepository: RealArtistRepository
) : LastAddedRepository {

    private fun getAllRecentSongs(): List<Song> {
        return songRepository.songs(makeLastAddedCursor())
    }

    override fun recentSongs(): List<Song> {
        return getAllRecentSongs().take(smartPlaylistLimit)
    }

    private fun getAllRecentAlbums(): List<Album> {
        return albumRepository.splitIntoAlbums(getAllRecentSongs())
    }

    override fun recentAlbums(): List<Album> {
        return getAllRecentAlbums().take(smartPlaylistLimit)
    }

    private fun getAllRecentArtist(): List<Artist> {
        return artistRepository.splitIntoArtists(recentAlbums())
    }

    override fun recentArtists(): List<Artist> {
        return getAllRecentArtist().take(smartPlaylistLimit)
    }

    private fun makeLastAddedCursor(): Cursor? {
        return songRepository.makeSongCursor(
            null,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )
    }
}
