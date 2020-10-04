package com.o4x.musical.model.smartplaylist

import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.model.Song
import kotlinx.android.parcel.Parcelize

@Parcelize
class LastAddedPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.last_added),
    iconRes = R.drawable.ic_library_add_white_24dp
) {
    override fun songs(): List<Song> {
        return lastAddedRepository.recentSongs()
    }
}