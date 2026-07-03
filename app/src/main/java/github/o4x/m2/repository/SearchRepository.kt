package github.o4x.m2.repository

import android.content.Context
import github.o4x.m2.R
import github.o4x.m2.model.Genre
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
            coroutineScope {
                val songsDeferred = async { songRepository.songs(searchString) }
                val artistsDeferred = async { artistRepository.artists(searchString) }
                val albumsDeferred = async { albumRepository.albums(searchString) }

                val songs = songsDeferred.await()
                val artists = artistsDeferred.await()
                val albums = albumsDeferred.await()

                if (songs.isNotEmpty()) {
                    results.add(context.resources.getString(R.string.songs))
                    results.addAll(songs)
                }
                if (artists.isNotEmpty()) {
                    results.add(context.resources.getString(R.string.artists))
                    results.addAll(artists)
                }
                if (albums.isNotEmpty()) {
                    results.add(context.resources.getString(R.string.albums))
                    results.addAll(albums)
                }
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
