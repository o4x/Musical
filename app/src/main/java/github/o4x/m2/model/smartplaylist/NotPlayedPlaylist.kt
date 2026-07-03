package github.o4x.m2.model.smartplaylist

import github.o4x.m2.App
import github.o4x.m2.R
import github.o4x.m2.model.Song
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.runBlocking

@Parcelize
class NotPlayedPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.not_recently_played),
    iconRes = R.drawable.ic_music_off
) {
    override fun songs(): List<Song> {
        return runBlocking { roomRepository.notRecentlyPlayedTracks() }
    }
}
