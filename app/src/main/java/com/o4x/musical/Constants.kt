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

package com.o4x.musical

import android.provider.BaseColumns
import android.provider.MediaStore

object Constants {
    const val CLEAN_VERSION_PRODUCT_ID = "clean"
    const val GOOGLE_PLAY_LICENSING_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgY4/kD68775CJojiX7E12sudUmzn9wAt47vQV41ObL41ASadpezl34KBRRVH0Pxebm53+uAWlKRrEvZxhL9LeGsEMOyfekOBhoppgOMY0V9lA1sh85cxCkuRi0FDx27XcBB/lQhrT7IFYcZD7Fl18Wj7XIdaftmA9pwUxIB9BojCZfArBsG6cj64sJd0aM54zTUAkFvtRN3kAfnw/ZH7UhdhkHVouHWmJOn9jZFbhnMuJnDE1F8BGSm5iUnqnI+HmFItx6zl5lfEtTMV112nquRD9pv3ID1BUg0VD+C+9Z+NhnOZxPVz66gsOdI46vuvACqQncu3y5AU39K3MRAC/QIDAQAB"

    const val IS_MUSIC =
        MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''"

    @JvmStatic
    val baseProjection = arrayOf(
        BaseColumns._ID, // 0
        MediaStore.Audio.AudioColumns.TITLE, // 1
        MediaStore.Audio.AudioColumns.TRACK, // 2
        MediaStore.Audio.AudioColumns.YEAR, // 3
        MediaStore.Audio.AudioColumns.DURATION, // 4
        MediaStore.Audio.AudioColumns.DATA, // 5
        MediaStore.Audio.AudioColumns.DATE_MODIFIED, // 6
        MediaStore.Audio.AudioColumns.ALBUM_ID, // 7
        MediaStore.Audio.AudioColumns.ALBUM, // 8
        MediaStore.Audio.AudioColumns.ARTIST_ID, // 9
        MediaStore.Audio.AudioColumns.ARTIST,// 10
        MediaStore.Audio.AudioColumns.COMPOSER,// 11
        "album_artist"//12
    )
}