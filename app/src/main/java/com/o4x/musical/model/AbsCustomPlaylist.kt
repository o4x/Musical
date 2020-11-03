package com.o4x.musical.model

import com.o4x.musical.repository.LastAddedRepository
import com.o4x.musical.repository.SongRepository
import com.o4x.musical.repository.TopPlayedRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class AbsCustomPlaylist(
    id: Long,
    name: String
) : Playlist(id, name), KoinComponent {

    override fun songs(): List<Song> {
        return emptyList()
    }

    protected val songRepository by inject<SongRepository>()

    protected val topPlayedRepository by inject<TopPlayedRepository>()

    protected val lastAddedRepository by inject<LastAddedRepository>()
}