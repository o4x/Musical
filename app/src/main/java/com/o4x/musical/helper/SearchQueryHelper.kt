package com.o4x.musical.helper

import android.app.SearchManager
import android.os.Bundle
import android.provider.MediaStore
import com.o4x.musical.model.Song
import com.o4x.musical.repository.SongRepository
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

object SearchQueryHelper : KoinComponent {
    private const val TITLE_SELECTION = "lower(" + MediaStore.Audio.AudioColumns.TITLE + ") = ?"
    private const val ALBUM_SELECTION = "lower(" + MediaStore.Audio.AudioColumns.ALBUM + ") = ?"
    private const val ARTIST_SELECTION = "lower(" + MediaStore.Audio.AudioColumns.ARTIST + ") = ?"
    private const val AND = " AND "
    private val songRepository by inject<SongRepository>()
    var songs = ArrayList<Song>()

    @JvmStatic
    fun getSongs(extras: Bundle): List<Song> {
        val query = extras.getString(SearchManager.QUERY, null)
        val artistName = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST, null)
        val albumName = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM, null)
        val titleName = extras.getString(MediaStore.EXTRA_MEDIA_TITLE, null)

        var songs = listOf<Song>()
        if (artistName != null && albumName != null && titleName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    ARTIST_SELECTION + AND + ALBUM_SELECTION + AND + TITLE_SELECTION,
                    arrayOf(
                        artistName.toLowerCase(Locale.getDefault()),
                        albumName.toLowerCase(Locale.getDefault()),
                        titleName.toLowerCase(Locale.getDefault())
                    )
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (artistName != null && titleName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    ARTIST_SELECTION + AND + TITLE_SELECTION,
                    arrayOf(
                        artistName.toLowerCase(Locale.getDefault()),
                        titleName.toLowerCase(Locale.getDefault())
                    )
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (albumName != null && titleName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    ALBUM_SELECTION + AND + TITLE_SELECTION,
                    arrayOf(
                        albumName.toLowerCase(Locale.getDefault()),
                        titleName.toLowerCase(Locale.getDefault())
                    )
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (artistName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    ARTIST_SELECTION,
                    arrayOf(artistName.toLowerCase(Locale.getDefault()))
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (albumName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    ALBUM_SELECTION,
                    arrayOf(albumName.toLowerCase(Locale.getDefault()))
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        if (titleName != null) {
            songs = songRepository.songs(
                songRepository.makeSongCursor(
                    TITLE_SELECTION,
                    arrayOf(titleName.toLowerCase(Locale.getDefault()))
                )
            )
        }
        if (songs.isNotEmpty()) {
            return songs
        }
        songs = songRepository.songs(
            songRepository.makeSongCursor(
                ARTIST_SELECTION,
                arrayOf(query.toLowerCase(Locale.getDefault()))
            )
        )

        if (songs.isNotEmpty()) {
            return songs
        }
        songs = songRepository.songs(
            songRepository.makeSongCursor(
                ALBUM_SELECTION,
                arrayOf(query.toLowerCase(Locale.getDefault()))
            )
        )
        if (songs.isNotEmpty()) {
            return songs
        }
        songs = songRepository.songs(
            songRepository.makeSongCursor(
                TITLE_SELECTION,
                arrayOf(query.toLowerCase(Locale.getDefault()))
            )
        )
        return if (songs.isNotEmpty()) {
            songs
        } else ArrayList()
    }
}
