package github.o4x.m2.repository

import android.content.ContentUris
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Media
import android.util.Log
import github.o4x.m2.Constants.IS_MUSIC
import github.o4x.m2.Constants.baseProjection
import github.o4x.m2.extensions.getInt
import github.o4x.m2.extensions.getLong
import github.o4x.m2.extensions.getString
import github.o4x.m2.extensions.getStringOrNull
import github.o4x.m2.model.Song
import github.o4x.m2.prefs.PreferenceUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SongRepository(private val context: Context) {

    fun songs(): List<Song> {
        return songs(makeSongCursor(null, null))
    }

    /**
     * Streams all songs from a single cursor pass in growing snapshots: a small first
     * chunk so the list can render immediately, then larger increments until complete.
     * Always emits at least once (an empty list when the library is empty).
     */
    fun songsFlow(
        firstChunkSize: Int = FIRST_CHUNK_SIZE,
        chunkSize: Int = CHUNK_SIZE,
        sortOrder: String = PreferenceUtil.songSortOrder
    ): Flow<List<Song>> = flow {
        val songs = arrayListOf<Song>()
        var lastEmittedSize = -1
        makeSongCursor(null, null, sortOrder)?.use { cursor ->
            var nextEmitSize = firstChunkSize
            if (cursor.moveToFirst()) {
                do {
                    songs.add(getSongFromCursorImpl(cursor))
                    if (songs.size == nextEmitSize) {
                        emit(ArrayList(songs))
                        lastEmittedSize = songs.size
                        nextEmitSize = songs.size + chunkSize
                    }
                } while (cursor.moveToNext())
            }
        }
        if (songs.size != lastEmittedSize) {
            emit(ArrayList(songs))
        }
    }

    fun songs(cursor: Cursor?): List<Song> {
        val songs = arrayListOf<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    fun song(cursor: Cursor?): Song {
        val song: Song = if (cursor != null && cursor.moveToFirst()) {
            getSongFromCursorImpl(cursor)
        } else {
            Song.emptySong
        }
        cursor?.close()
        return song
    }

    fun songs(query: String): List<Song> {
        return songs(makeSongCursor(AudioColumns.TITLE + " LIKE ?", arrayOf("%$query%")))
    }

    fun song(songId: Long): Song {
        return song(makeSongCursor(AudioColumns._ID + "=?", arrayOf(songId.toString())))
    }

    fun songs(idList: LongArray): List<Song> {
        val cursor = makeSongCursor(makeSelection(idList), null)
        return if (cursor == null) {
            songs(cursor)
        } else {
            songs(SortedLongCursor(cursor, idList, AudioColumns._ID))
        }
    }

    fun songsByFilePath(filePath: String): List<Song> {
        return songs(
            makeSongCursor(
                AudioColumns.DATA + "=?",
                arrayOf(filePath)
            )
        )
    }

    private fun getSongFromCursorImpl(cursor: Cursor): Song {
        val id = cursor.getLong(AudioColumns._ID)
        val title = cursor.getString(AudioColumns.TITLE)
        val trackNumber = cursor.getInt(AudioColumns.TRACK)
        val year = cursor.getInt(AudioColumns.YEAR)
        val duration = cursor.getLong(AudioColumns.DURATION)

        // IMPORTANT: In Android 10+, DATA (File Path) is deprecated and often inaccessible.
        // It is better to generate a Content URI for the song.
        // However, if your Song model requires a string path, keep using DATA but be aware
        // you cannot use java.io.File(data) to read it. You must use ContentResolver.
        val data = cursor.getString(AudioColumns.DATA)

        // Generate a playable URI (Recommended approach for modern Android)
        val contentUri = ContentUris.withAppendedId(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id
        )

        val dateModified = cursor.getLong(AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(AudioColumns.ALBUM_ID)
        val albumName = cursor.getStringOrNull(AudioColumns.ALBUM)
        val artistId = cursor.getLong(AudioColumns.ARTIST_ID)
        val artistName = cursor.getStringOrNull(AudioColumns.ARTIST)
        val composer = cursor.getStringOrNull(AudioColumns.COMPOSER)

        // "album_artist" might not exist in older API levels columns, safe check
        val albumArtist = try {
            cursor.getStringOrNull("album_artist")
        } catch (e: Exception) { "" }

        return Song(
            id,
            title,
            trackNumber,
            year,
            duration,
            data, // Ideally pass contentUri.toString() here if your player supports URIs
            dateModified,
            albumId,
            albumName ?: "<unknown>",
            artistId,
            artistName ?: "<unknown>",
            composer ?: "",
            albumArtist ?: ""
        )
    }

    fun makeSelection(idList: LongArray): String {
        return "_id IN (${idList.joinToString(",")})"
    }

    fun makeLastAddedSongsCursor(limit: Int): Cursor? {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            Media.EXTERNAL_CONTENT_URI
        val args = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, IS_MUSIC)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(AudioColumns.DATE_ADDED))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        }
        return try {
            context.contentResolver.query(uri, baseProjection, args, null)
        } catch (e: Exception) {
            null
        }
    }

    @JvmOverloads
    fun makeSongCursor(
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String = PreferenceUtil.songSortOrder
    ): Cursor? {
        var selectionFinal = selection

        // In modern Android, explicitly selecting IS_MUSIC!=0 is good,
        // but ensure we don't break logic if selection is null
        val musicClause = "$IS_MUSIC != 0"

        if (selectionFinal != null && selectionFinal.trim().isNotEmpty()) {
            selectionFinal = "$musicClause AND $selectionFinal"
        } else {
            selectionFinal = musicClause
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            Media.EXTERNAL_CONTENT_URI
        }

        return try {
            context.contentResolver.query(
                uri,
                baseProjection,
                selectionFinal,
                selectionValues,
                sortOrder
            )
        } catch (ex: SecurityException) {
            Log.e("SongRepository", "Security Exception: Permission missing", ex)
            return null
        } catch (ex: Exception) {
            Log.e("SongRepository", "Generic Error", ex)
            return null
        }
    }

    companion object {
        private const val FIRST_CHUNK_SIZE = 50
        private const val CHUNK_SIZE = 100
    }
}
