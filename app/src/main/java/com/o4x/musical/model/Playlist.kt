package com.o4x.musical.model

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.o4x.musical.repository.Repository
import com.o4x.musical.util.MusicUtil
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers.IO
import org.koin.core.KoinComponent
import org.koin.core.inject

@Parcelize
open class Playlist(
    val id: Long,
    val name: String
) : Parcelable, KoinComponent {

    companion object {
        val empty = Playlist(-1, "")
    }

    val repository by inject<Repository>()

    open fun songs(): List<Song> {
        return repository.getPlaylistSongs(this)
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