/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package com.o4x.musical.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.o4x.musical.R
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
        SONGS(R.string.songs, R.drawable.ic_music_note_white_24dp),
        ALBUMS(R.string.albums, R.drawable.ic_album_white_24dp),
        ARTISTS(R.string.artists, R.drawable.ic_artist),
        PLAYLISTS(R.string.playlists, R.drawable.ic_queue_music_white_24dp),
        GENRES(R.string.genres, R.drawable.ic_guitar),
    }
}