package github.o4x.musical.model.smartplaylist

import github.o4x.musical.App
import github.o4x.musical.R
import github.o4x.musical.model.Song
import kotlinx.android.parcel.Parcelize

@Parcelize
class TopTracksPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.my_top_tracks),
    iconRes = R.drawable.ic_trending_up
) {
    override fun songs(): List<Song> {
        return roomRepository.playCountSongs()
    }
}