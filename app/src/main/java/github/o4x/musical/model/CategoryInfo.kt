package github.o4x.musical.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import github.o4x.musical.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategoryInfo(
    val category: Category,
    @get:JvmName("isVisible")
    var visible: Boolean
) : Parcelable {

    enum class Category(
        @StringRes val stringRes: Int,
        @DrawableRes val icon: Int
    ) {
        SONGS(R.string.songs, R.drawable.ic_music),
        ALBUMS(R.string.albums, R.drawable.ic_album),
        ARTISTS(R.string.artists, R.drawable.ic_artist),
        PLAYLISTS(R.string.playlists, R.drawable.ic_queue_music),
        GENRES(R.string.genres, R.drawable.ic_guitar),
    }
}