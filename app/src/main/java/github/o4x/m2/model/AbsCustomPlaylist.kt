package github.o4x.m2.model

import github.o4x.m2.repository.LastAddedRepository
import github.o4x.m2.repository.RoomRepository
import github.o4x.m2.repository.SongRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


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