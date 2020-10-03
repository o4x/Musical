package com.o4x.musical.loader

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import com.o4x.musical.extensions.getInt
import com.o4x.musical.extensions.getLong
import com.o4x.musical.extensions.getString
import com.o4x.musical.extensions.getStringOrNull
import com.o4x.musical.model.Song
import com.o4x.musical.model.Song.Companion.emptySong
import com.o4x.musical.provider.BlacklistStore
import com.o4x.musical.util.PreferenceUtil.songSortOrder
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
object SongLoader {
    const val BASE_SELECTION =
        MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''"
    @JvmField
    val BASE_PROJECTION = arrayOf(
        BaseColumns._ID,  // 0
        MediaStore.Audio.AudioColumns.TITLE,  // 1
        MediaStore.Audio.AudioColumns.TRACK,  // 2
        MediaStore.Audio.AudioColumns.YEAR,  // 3
        MediaStore.Audio.AudioColumns.DURATION,  // 4
        MediaStore.Audio.AudioColumns.DATA,  // 5
        MediaStore.Audio.AudioColumns.DATE_MODIFIED,  // 6
        MediaStore.Audio.AudioColumns.ALBUM_ID,  // 7
        MediaStore.Audio.AudioColumns.ALBUM,  // 8
        MediaStore.Audio.AudioColumns.ARTIST_ID,  // 9
        MediaStore.Audio.AudioColumns.ARTIST)

    @JvmStatic
    fun getAllSongs(context: Context): List<Song> {
        val cursor = makeSongCursor(context, null, null)
        return getSongs(cursor)
    }

    @JvmStatic
    fun getSongs(context: Context, query: String): List<Song> {
        val cursor =
            makeSongCursor(context, MediaStore.Audio.AudioColumns.TITLE + " LIKE ?", arrayOf(
                "%$query%"))
        return getSongs(cursor)
    }

    @JvmStatic
    fun getSong(context: Context, queryId: Long): Song {
        val cursor = makeSongCursor(context,
            MediaStore.Audio.AudioColumns._ID + "=?",
            arrayOf(queryId.toString()))
        return getSong(cursor)
    }

    @JvmStatic
    fun getSongs(cursor: Cursor?): List<Song> {
        val songs: MutableList<Song> = ArrayList()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    fun getSong(cursor: Cursor?): Song {
        val song: Song = if (cursor != null && cursor.moveToFirst()) {
            getSongFromCursorImpl(cursor)
        } else {
            emptySong
        }
        cursor?.close()
        return song
    }

    private fun getSongFromCursorImpl(
        cursor: Cursor
    ): Song {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val title = cursor.getString(MediaStore.Audio.AudioColumns.TITLE)
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK)
        val year = cursor.getInt(MediaStore.Audio.AudioColumns.YEAR)
        val duration = cursor.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val data = cursor.getString(MediaStore.Audio.AudioColumns.DATA)
        val dateModified = cursor.getLong(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val albumName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ALBUM)
        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val artistName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ARTIST)
        val composer = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.COMPOSER)
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

    @JvmStatic
    @JvmOverloads
    fun makeSongCursor(
        context: Context,
        selection: String?,
        selectionValues: Array<String?>?,
        sortOrder: String? = songSortOrder
    ): Cursor? {
        var selection = selection
        var selectionValues = selectionValues
        selection = if (selection != null && selection.trim { it <= ' ' } != "") {
            BASE_SELECTION + " AND " + selection
        } else {
            BASE_SELECTION
        }

        // Blacklist
        val paths: List<String> = BlacklistStore.getInstance(context).paths
        if (!paths.isEmpty()) {
            selection = generateBlacklistSelection(selection, paths.size)
            selectionValues = addBlacklistSelectionValues(selectionValues, paths)
        }
        return try {
            context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                BASE_PROJECTION, selection, selectionValues, sortOrder)
        } catch (e: SecurityException) {
            null
        }
    }

    private fun generateBlacklistSelection(selection: String?, pathCount: Int): String {
        var newSelection =
            if (selection != null && selection.trim { it <= ' ' } != "") "$selection AND " else ""
        newSelection += MediaStore.Audio.AudioColumns.DATA + " NOT LIKE ?"
        for (i in 0 until pathCount - 1) {
            newSelection += " AND " + MediaStore.Audio.AudioColumns.DATA + " NOT LIKE ?"
        }
        return newSelection
    }

    private fun addBlacklistSelectionValues(
        selectionValues: Array<String?>?,
        paths: List<String>
    ): Array<String?> {
        var selectionValues = selectionValues
        if (selectionValues == null) selectionValues = arrayOfNulls(0)
        val newSelectionValues = arrayOfNulls<String>(selectionValues.size + paths.size)
        System.arraycopy(selectionValues, 0, newSelectionValues, 0, selectionValues.size)
        for (i in selectionValues.size until newSelectionValues.size) {
            newSelectionValues[i] = paths[i - selectionValues.size] + "%"
        }
        return newSelectionValues
    }
}