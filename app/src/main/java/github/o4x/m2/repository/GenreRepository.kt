package github.o4x.m2.repository

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore.Audio.Genres
import github.o4x.m2.Constants.IS_MUSIC
import github.o4x.m2.Constants.baseProjection
import github.o4x.m2.extensions.getLong
import github.o4x.m2.extensions.getString
import github.o4x.m2.extensions.getStringOrNull
import github.o4x.m2.model.Genre
import github.o4x.m2.model.Song
import github.o4x.m2.prefs.PreferenceUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GenreRepository(
    private val contentResolver: ContentResolver,
    private val songRepository: SongRepository
) {

    fun genres(): List<Genre> {
        return getGenresFromCursor(makeGenreCursor())
    }

    /**
     * Streams genres in growing snapshots. Building each genre needs its own
     * MediaStore members query, so emitting every few genres lets the list
     * render long before the last genre is resolved.
     * Always emits at least once (an empty list when there are no genres).
     */
    fun genresFlow(chunkSize: Int = GENRE_CHUNK_SIZE): Flow<List<Genre>> = flow {
        val genres = arrayListOf<Genre>()
        var lastEmittedSize = -1
        makeGenreCursor()?.use { cursor ->
            if (cursor.moveToFirst()) {
                val songCountMap = loadGenreSongCounts()
                do {
                    val genre = getGenreFromCursorWithCount(cursor, songCountMap)
                    if (genre.songCount > 0) {
                        genres.add(genre)
                        if (genres.size % chunkSize == 0) {
                            emit(ArrayList(genres))
                            lastEmittedSize = genres.size
                        }
                    } else {
                        deleteEmptyGenre(genre.id)
                    }
                } while (cursor.moveToNext())
            }
        }
        if (genres.size != lastEmittedSize) {
            emit(ArrayList(genres))
        }
    }

    fun songs(genreId: Long): List<Song> {
        // The genres table only stores songs that have a genre specified,
        // so we need to get songs without a genre a different way.
        return if (genreId == -1L) {
            getSongsWithNoGenre()
        } else songRepository.songs(makeGenreSongCursor(genreId))
    }

    private fun getGenreFromCursorWithCount(cursor: Cursor, songCountMap: Map<Long, Int>): Genre {
        val id = cursor.getLong(Genres._ID)
        val name = cursor.getStringOrNull(Genres.NAME)
        val songCount = songCountMap[id] ?: 0
        // Skip loading songs for empty genres (saves N IPC calls for genres being deleted).
        val songs = if (songCount > 0) songRepository.songs(makeGenreSongCursor(id)) else emptyList()
        return Genre(id, name ?: "", songs, songCount)
    }

    private fun getGenreFromCursorWithOutSongs(cursor: Cursor): Genre {
        val id = cursor.getLong(Genres._ID)
        val name = cursor.getString(Genres.NAME)
        return Genre(id, name, ArrayList<Song>(), -1)
    }

    private fun getSongsWithNoGenre(): List<Song> {
        val selection =
            BaseColumns._ID + " NOT IN " + "(SELECT " + Genres.Members.AUDIO_ID + " FROM audio_genres_map)"
        return songRepository.songs(songRepository.makeSongCursor(selection, null))
    }

    private fun hasSongsWithNoGenre(): Boolean {
        val allSongsCursor = songRepository.makeSongCursor(null, null)
        val allSongsWithGenreCursor = makeAllSongsWithGenreCursor()

        if (allSongsCursor == null || allSongsWithGenreCursor == null) {
            return false
        }

        val hasSongsWithNoGenre = allSongsCursor.count > allSongsWithGenreCursor.count
        allSongsCursor.close()
        allSongsWithGenreCursor.close()
        return hasSongsWithNoGenre
    }

    private fun makeAllSongsWithGenreCursor(): Cursor? {
        println(Genres.EXTERNAL_CONTENT_URI.toString())
        return contentResolver.query(
            Uri.parse("content://media/external/audio/genres/all/members"),
            arrayOf(Genres.Members.AUDIO_ID), null, null, null
        )
    }

    private fun makeGenreSongCursor(genreId: Long): Cursor? {
        return contentResolver.query(
            Genres.Members.getContentUri("external", genreId),
            baseProjection,
            IS_MUSIC,
            null,
            PreferenceUtil.songSortOrder
        )
    }

    private fun loadGenreSongCounts(): Map<Long, Int> {
        val countMap = mutableMapOf<Long, Int>()
        val cursor = contentResolver.query(
            android.net.Uri.parse("content://media/external/audio/genres/all/members"),
            arrayOf(Genres.Members.GENRE_ID),
            null, null, null
        ) ?: return countMap
        cursor.use {
            while (it.moveToNext()) {
                val genreId = it.getLong(0)
                countMap[genreId] = (countMap[genreId] ?: 0) + 1
            }
        }
        return countMap
    }

    private fun getGenresFromCursor(cursor: Cursor?): ArrayList<Genre> {
        val genres = arrayListOf<Genre>()
        if (cursor != null) {
            val songCountMap = loadGenreSongCounts()
            if (cursor.moveToFirst()) {
                do {
                    val genre = getGenreFromCursorWithCount(cursor, songCountMap)
                    if (genre.songCount > 0) {
                        genres.add(genre)
                    } else {
                        deleteEmptyGenre(genre.id)
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return genres
    }

    private fun getGenresFromCursorForSearch(cursor: Cursor?): List<Genre> {
        val genres = mutableListOf<Genre>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                genres.add(getGenreFromCursorWithOutSongs(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return genres
    }


    private fun deleteEmptyGenre(genreId: Long) {
        try {
            contentResolver.delete(
                Genres.EXTERNAL_CONTENT_URI,
                Genres._ID + " == " + genreId,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun makeGenreCursor(): Cursor? {
        val projection = arrayOf(Genres._ID, Genres.NAME)
        return contentResolver.query(
            Genres.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            PreferenceUtil.genreSortOrder
        )
    }

    companion object {
        private const val GENRE_CHUNK_SIZE = 5
    }
}
