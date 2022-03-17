package github.o4x.musical.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Genre(
    val id: Long,
    val name: String,
    val songs: List<Song>,
    val songCount: Int
) : Parcelable