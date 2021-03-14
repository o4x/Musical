package com.o4x.musical.repository

import android.content.Context
import com.o4x.musical.R
import com.o4x.musical.model.Genre
import java.util.*

class SearchRepository(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val roomRepository: RoomRepository,
    private val genreRepository: GenreRepository,
) {
    suspend fun searchAll(context: Context, query: String?): MutableList<Any> {
        val results = mutableListOf<Any>()
        query?.let { searchString ->
            val songs = songRepository.songs(searchString)
            if (songs.isNotEmpty()) {
                results.add(context.resources.getString(R.string.songs))
                results.addAll(songs)
            }
            val artists = artistRepository.artists(searchString)
            if (artists.isNotEmpty()) {
                results.add(context.resources.getString(R.string.artists))
                results.addAll(artists)
            }

            val albums = albumRepository.albums(searchString)
            if (albums.isNotEmpty()) {
                results.add(context.resources.getString(R.string.albums))
                results.addAll(albums)
            }
//            val genres: List<Genre> = genreRepository.genres().filter { genre ->
//                genre.name.toLowerCase(Locale.getDefault())
//                    .contains(searchString.toLowerCase(Locale.getDefault()))
//            }
//            if (genres.isNotEmpty()) {
//                results.add(context.resources.getString(R.string.genres))
//                results.addAll(genres)
//            }
//            val playlist = roomRepository.playlistWithSongs().filter { playlist ->
//                playlist.playlistEntity.playlistName.toLowerCase(Locale.getDefault())
//                    .contains(searchString.toLowerCase(Locale.getDefault()))
//            }
//            if (playlist.isNotEmpty()) {
//                results.add(context.getString(R.string.playlists))
//                results.addAll(playlist)
//            }
        }
        return results
    }
}
