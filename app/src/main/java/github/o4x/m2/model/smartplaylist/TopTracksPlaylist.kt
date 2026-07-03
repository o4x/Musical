package github.o4x.m2.model.smartplaylist

import github.o4x.m2.App
import github.o4x.m2.R
import github.o4x.m2.model.Song
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.runBlocking

@Parcelize
class TopTracksPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.my_top_tracks),
    iconRes = R.drawable.ic_trending_up
) {
    override fun songs(): List<Song> {
        return runBlocking { roomRepository.playCountSongs() }
    }
}
