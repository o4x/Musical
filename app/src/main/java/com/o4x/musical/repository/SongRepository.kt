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

package com.o4x.musical.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Media
import com.o4x.musical.Constants.IS_MUSIC
import com.o4x.musical.Constants.baseProjection
import com.o4x.musical.extensions.getInt
import com.o4x.musical.extensions.getLong
import com.o4x.musical.extensions.getString
import com.o4x.musical.extensions.getStringOrNull
import com.o4x.musical.model.Song
import com.o4x.musical.util.PreferenceUtil

/**
 * Created by hemanths on 10/08/17.
 */
interface SongRepository {

    fun songs(): List<Song>

    fun songs(cursor: Cursor?): List<Song>

    fun songs(query: String): List<Song>

    fun songs(idList: LongArray): List<Song>

    fun songsByFilePath(filePath: String): List<Song>

    fun song(cursor: Cursor?): Song

    fun song(songId: Long): Song
}

class RealSongRepository(private val context: Context) : SongRepository {

    override fun songs(): List<Song> {
        return songs(makeSongCursor(null, null))
    }

    override fun songs(cursor: Cursor?): List<Song> {
        val songs = arrayListOf<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    override fun song(cursor: Cursor?): Song {
        val song: Song = if (cursor != null && cursor.moveToFirst()) {
            getSongFromCursorImpl(cursor)
        } else {
            Song.emptySong
        }
        cursor?.close()
        return song
    }

    override fun songs(query: String): List<Song> {
        return songs(makeSongCursor(AudioColumns.TITLE + " LIKE ?", arrayOf("%$query%")))
    }

    override fun song(songId: Long): Song {
        return song(makeSongCursor(AudioColumns._ID + "=?", arrayOf(songId.toString())))
    }

    override fun songs(idList: LongArray): List<Song> {
        var selection = "_id IN ("
        for (id in idList) {
            selection += "$id,"
        }
        if (idList.isNotEmpty()) {
            selection = selection.substring(0, selection.length - 1)
        }
        selection += ")"

        return songs(makeSongCursor(selection, null))
    }

    override fun songsByFilePath(filePath: String): List<Song> {
        return songs(
            makeSongCursor(
                AudioColumns.DATA + "=?",
                arrayOf(filePath)
            )
        )
    }

    private fun getSongFromCursorImpl(
        cursor: Cursor
    ): Song {
        val id = cursor.getLong(AudioColumns._ID)
        val title = cursor.getString(AudioColumns.TITLE)
        val trackNumber = cursor.getInt(AudioColumns.TRACK)
        val year = cursor.getInt(AudioColumns.YEAR)
        val duration = cursor.getLong(AudioColumns.DURATION)
        val data = cursor.getString(AudioColumns.DATA)
        val dateModified = cursor.getLong(AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(AudioColumns.ALBUM_ID)
        val albumName = cursor.getStringOrNull(AudioColumns.ALBUM)
        val artistId = cursor.getLong(AudioColumns.ARTIST_ID)
        val artistName = cursor.getStringOrNull(AudioColumns.ARTIST)
        val composer = cursor.getStringOrNull(AudioColumns.COMPOSER)
        val albumArtist = cursor.getStringOrNull("album_artist")
        return Song(
            id,
            title,
            trackNumber,
            year,
            duration,
            data,
            dateModified,
            albumId,
            albumName ?: "",
            artistId,
            artistName ?: "",
            composer ?: "",
            albumArtist ?: ""
        )
    }

    @JvmOverloads
    fun makeSongCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.songSortOrder
    ): Cursor? {
        var selectionFinal = selection
        var selectionValuesFinal = selectionValues

        selectionFinal = if (selection != null && selection.trim { it <= ' ' } != "") {
            "$IS_MUSIC AND $selectionFinal"
        } else {
            IS_MUSIC
        }


        selectionFinal =
            selectionFinal + " AND " + Media.DURATION + ">= " + (PreferenceUtil.filterLength * 1000)

        val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            Media.EXTERNAL_CONTENT_URI
        }
        return try {
            context.contentResolver.query(
                uri,
                baseProjection,
                selectionFinal,
                selectionValuesFinal,
                sortOrder,
            )
        } catch (ex: SecurityException) {
            return null
        }
    }
}
