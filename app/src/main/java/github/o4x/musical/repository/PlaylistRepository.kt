package github.o4x.musical.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Playlists.*
import android.provider.MediaStore.Audio.PlaylistsColumns
import github.o4x.musical.Constants
import github.o4x.musical.extensions.getInt
import github.o4x.musical.extensions.getLong
import github.o4x.musical.extensions.getString
import github.o4x.musical.extensions.getStringOrNull
import github.o4x.musical.model.Playlist
import github.o4x.musical.model.PlaylistSong
import github.o4x.musical.model.Song

class PlaylistRepository(
    private val contentResolver: ContentResolver
) {

    fun playlist(cursor: Cursor?): Playlist {
        return cursor.use {
            if (cursor?.moveToFirst() == true) {
                getPlaylistFromCursorImpl(cursor)
            } else {
                Playlist.empty
            }
        }
    }

    fun playlist(playlistName: String): Playlist {
        return playlist(makePlaylistCursor(PlaylistsColumns.NAME + "=?", arrayOf(playlistName)))
    }

    fun playlist(playlistId: Long): Playlist {
        return playlist(
            makePlaylistCursor(
                BaseColumns._ID + "=?",
                arrayOf(playlistId.toString())
            )
        )
    }

    fun searchPlaylist(query: String): List<Playlist> {
        return playlists(makePlaylistCursor(PlaylistsColumns.NAME + "=?", arrayOf(query)))
    }

    fun playlists(): List<Playlist> {
        return playlists(makePlaylistCursor(null, null))
    }

    fun playlists(cursor: Cursor?): List<Playlist> {
        val playlists = mutableListOf<Playlist>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlists.add(getPlaylistFromCursorImpl(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return playlists
    }

    fun favoritePlaylist(playlistName: String): List<Playlist> {
        return playlists(
            makePlaylistCursor(
                PlaylistsColumns.NAME + "=?",
                arrayOf(playlistName)
            )
        )
    }

    fun deletePlaylist(playlistId: Long) {
        val localUri = EXTERNAL_CONTENT_URI
        val localStringBuilder = StringBuilder()
        localStringBuilder.append("_id IN (")
        localStringBuilder.append(playlistId)
        localStringBuilder.append(")")
        contentResolver.delete(localUri, localStringBuilder.toString(), null)
    }

    private fun getPlaylistFromCursorImpl(
        cursor: Cursor
    ): Playlist {
        val id = cursor.getLong(MediaStore.MediaColumns._ID)
        val name = cursor.getString(NAME)
        return Playlist(id, name)
    }

    fun playlistSongs(playlistId: Long): List<Song> {
        val songs = arrayListOf<Song>()
        val cursor = makePlaylistSongCursor(playlistId)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getPlaylistSongFromCursorImpl(cursor, playlistId))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    private fun getPlaylistSongFromCursorImpl(cursor: Cursor, playlistId: Long): PlaylistSong {
        val id = cursor.getLong(Members.AUDIO_ID)
        val title = cursor.getString(AudioColumns.TITLE)
        val trackNumber = cursor.getInt(AudioColumns.TRACK)
        val year = cursor.getInt(AudioColumns.YEAR)
        val duration = cursor.getLong(AudioColumns.DURATION)
        val data = cursor.getString(AudioColumns.DATA)
        val dateModified = cursor.getLong(AudioColumns.DATE_MODIFIED)
        val albumId = cursor.getLong(AudioColumns.ALBUM_ID)
        val albumName = cursor.getString(AudioColumns.ALBUM)
        val artistId = cursor.getLong(AudioColumns.ARTIST_ID)
        val artistName = cursor.getString(AudioColumns.ARTIST)
        val idInPlaylist = cursor.getLong(Members._ID)
        val composer = cursor.getStringOrNull(AudioColumns.COMPOSER)
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
            composer ?: "",
            albumArtist
        )
    }

    private fun makePlaylistCursor(
        selection: String?,
        values: Array<String>?
    ): Cursor? {
        return contentResolver.query(
            EXTERNAL_CONTENT_URI,
            arrayOf(
                BaseColumns._ID, /* 0 */
                PlaylistsColumns.NAME /* 1 */
            ),
            selection,
            values,
            DEFAULT_SORT_ORDER
        )
    }


    private fun makePlaylistSongCursor(playlistId: Long): Cursor? {
        return contentResolver.query(
            Members.getContentUri("external", playlistId),
            arrayOf(
                Members.AUDIO_ID, // 0
                AudioColumns.TITLE, // 1
                AudioColumns.TRACK, // 2
                AudioColumns.YEAR, // 3
                AudioColumns.DURATION, // 4
                AudioColumns.DATA, // 5
                AudioColumns.DATE_MODIFIED, // 6
                AudioColumns.ALBUM_ID, // 7
                AudioColumns.ALBUM, // 8
                AudioColumns.ARTIST_ID, // 9
                AudioColumns.ARTIST, // 10
                Members._ID,//11
                AudioColumns.COMPOSER,//12
                "album_artist"//13
            ), Constants.IS_MUSIC, null, Members.DEFAULT_SORT_ORDER
        )
    }
}
