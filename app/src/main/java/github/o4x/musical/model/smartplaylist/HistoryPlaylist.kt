package github.o4x.musical.model.smartplaylist

import github.o4x.musical.App
import github.o4x.musical.R
import github.o4x.musical.model.Song
import kotlinx.android.parcel.Parcelize
import org.koin.core.KoinComponent

@Parcelize
class HistoryPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.history),
    iconRes = R.drawable.ic_history
), KoinComponent {
    override fun songs(): List<Song> {
        return roomRepository.historySongs()
    }
}