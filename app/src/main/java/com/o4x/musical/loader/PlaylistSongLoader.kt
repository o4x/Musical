package com.o4x.musical.loader

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.o4x.musical.Constants.IS_MUSIC
import com.o4x.musical.extensions.getInt
import com.o4x.musical.extensions.getLong
import com.o4x.musical.extensions.getString
import com.o4x.musical.extensions.getStringOrNull
import com.o4x.musical.model.PlaylistSong
import java.util.*

object PlaylistSongLoader {
    @JvmStatic
    fun getPlaylistSongList(context: Context, playlistId: Long): List<PlaylistSong> {
        val songs: MutableList<PlaylistSong> = ArrayList()
        val cursor = makePlaylistSongCursor(context, playlistId)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getPlaylistSongFromCursorImpl(cursor, playlistId))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    // TODO duplicated in [PlaylistRepository.kt]
    private fun getPlaylistSongFromCursorImpl(cursor: Cursor, playlistId: Long): PlaylistSong {
        val id = cursor.getLong(MediaStore.Audio.Playlists.Members.AUDIO_ID)
        val title = cursor.getString(MediaStore.Audio.AudioColumns.TITLE)
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK)
        val year = cursor.getInt(MediaStore.Audio.AudioColumns.YEAR)
        val duration = cursor.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val data = cursor.getString(MediaStore.Audio.AudioColumns.DATA)
        val dateModified = cursor.getLong(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val albumName = cursor.getString(MediaStore.Audio.AudioColumns.ALBUM)
        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val artistName = cursor.getString(MediaStore.Audio.AudioColumns.ARTIST)
        val idInPlaylist = cursor.getLong(MediaStore.Audio.Playlists.Members._ID)
        val composer = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.COMPOSER)
        val albumArtist = cursor.getStringOrNull("album_artist")
        return PlaylistSong(
            id,
            title,
            trackNumber,
            year,
            duration,
            data,
            dateModified,
            albumId,
            albumName,
            artistId,
            artistName,
            playlistId,
            idInPlaylist,
            composer,
            albumArtist
        )
    }

    fun makePlaylistSongCursor(context: Context, playlistId: Long): Cursor? {
        return try {
            context.contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId), arrayOf(
                    MediaStore.Audio.Playlists.Members.AUDIO_ID,  // 0
                    MediaStore.Audio.AudioColumns.TITLE,  // 1
                    MediaStore.Audio.AudioColumns.TRACK,  // 2
                    MediaStore.Audio.AudioColumns.YEAR,  // 3
                    MediaStore.Audio.AudioColumns.DURATION,  // 4
                    MediaStore.Audio.AudioColumns.DATA,  // 5
                    MediaStore.Audio.AudioColumns.DATE_MODIFIED,  // 6
                    MediaStore.Audio.AudioColumns.ALBUM_ID,  // 7
                    MediaStore.Audio.AudioColumns.ALBUM,  // 8
                    MediaStore.Audio.AudioColumns.ARTIST_ID,  // 9
                    MediaStore.Audio.AudioColumns.ARTIST,  // 10
                    MediaStore.Audio.Playlists.Members._ID // 11
                ), IS_MUSIC, null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER)
        } catch (e: SecurityException) {
            null
        }
    }
}