package com.o4x.musical.model

import android.content.Context
import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import com.o4x.musical.repository.RealPlaylistRepository
import com.o4x.musical.util.MusicUtil
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.get

@Parcelize
open class Playlist(
    val id: Long,
    val name: String
) : Parcelable, KoinComponent {

    companion object {
        val empty = Playlist(-1, "")
    }

    open fun songs(): List<Song> {
        return RealPlaylistRepository(get()).playlistSongs(id)
    }

    fun getSongsLive(): LiveData<List<Song>> = liveData(IO) {
        emit(songs())
    }

    open fun getInfoString(context: Context): String {
        val songCount = songs().size
        return  MusicUtil.getSongCountString(context, songCount)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Playlist

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }


}