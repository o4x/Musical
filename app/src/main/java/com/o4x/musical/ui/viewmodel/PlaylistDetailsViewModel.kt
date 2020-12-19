/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.o4x.musical.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.model.Playlist
import com.o4x.musical.model.Song
import com.o4x.musical.repository.Repository
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
