package github.o4x.m2.model.smartplaylist

import github.o4x.m2.App
import github.o4x.m2.R
import github.o4x.m2.model.Song
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent

@Parcelize
class HistoryPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.history),
    iconRes = R.drawable.ic_history
), KoinComponent {
    override fun songs(): List<Song> {
        return runBlocking { roomRepository.historySongs() }
    }
}
