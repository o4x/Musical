package com.o4x.musical.model

import com.o4x.musical.repository.LastAddedRepository
import com.o4x.musical.repository.RoomRepository
import com.o4x.musical.repository.SongRepository
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

    protected val roomRepository by inject<RoomRepository>()

    protected val lastAddedRepository by inject<LastAddedRepository>()
}