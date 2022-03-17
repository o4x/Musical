package github.o4x.musical.model.smartplaylist

import github.o4x.musical.App
import github.o4x.musical.R
import github.o4x.musical.model.Song
import kotlinx.android.parcel.Parcelize

@Parcelize
class NotPlayedPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.not_recently_played),
    iconRes = R.drawable.ic_music_off
) {
    override fun songs(): List<Song> {
        return roomRepository.notRecentlyPlayedTracks()
    }
}