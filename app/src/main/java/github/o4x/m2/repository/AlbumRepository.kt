package github.o4x.m2.repository

import android.provider.MediaStore.Audio.AudioColumns
import github.o4x.m2.helper.SortOrder
import github.o4x.m2.model.Album
import github.o4x.m2.model.Song
import github.o4x.m2.prefs.PreferenceUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map

class AlbumRepository(private val songRepository: SongRepository) {

    fun albums(): List<Album> {
        val songs = songRepository.songs(
            songRepository.makeSongCursor(
                null,
                null,
                getSongLoaderSortOrder()
            )
        )
        return splitIntoAlbums(songs)
    }

    /**
     * Streams the album list in growing snapshots as the underlying songs load.
     * Conflated so regrouping only happens as fast as the collector keeps up;
     * the last emission covers all songs.
     */
    fun albumsFlow(): Flow<List<Album>> =
        songRepository.songsFlow(sortOrder = getSongLoaderSortOrder())
            .conflate()
            .map { splitIntoAlbums(it) }

    fun albums(query: String): List<Album> {
        val songs = songRepository.songs(
            songRepository.makeSongCursor(
                AudioColumns.ALBUM + " LIKE ?",
                arrayOf("%$query%"),
                getSongLoaderSortOrder()
            )
        )
        return splitIntoAlbums(songs)
    }

    fun album(albumId: Long): Album {
        val cursor = songRepository.makeSongCursor(
            AudioColumns.ALBUM_ID + "=?",
            arrayOf(albumId.toString()),
            getSongLoaderSortOrder()
        )
        val songs = songRepository.songs(cursor)
        val album = Album(albumId, songs)
        sortAlbumSongs(album)
        return album
    }

    fun splitIntoAlbums(
        songs: List<Song>
    ): List<Album> {
        val sortOrder = PreferenceUtil.albumSongSortOrder
        return songs.groupBy { it.albumId }
            .map { sortAlbumSongs(Album(it.key, it.value), sortOrder) }
    }

    private fun sortAlbumSongs(album: Album, sortOrder: String? = PreferenceUtil.albumSongSortOrder): Album {
        val songs = when (sortOrder) {
            SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST -> album.songs.sortedWith { o1, o2 ->
                o1.trackNumber.compareTo(o2.trackNumber)
            }
            SortOrder.AlbumSongSortOrder.SONG_A_Z -> album.songs.sortedWith { o1, o2 ->
                o1.title.compareTo(o2.title)
            }
            SortOrder.AlbumSongSortOrder.SONG_Z_A -> album.songs.sortedWith { o1, o2 ->
                o2.title.compareTo(o1.title)
            }
            SortOrder.AlbumSongSortOrder.SONG_DURATION -> album.songs.sortedWith { o1, o2 ->
                o1.duration.compareTo(o2.duration)
            }
            else -> throw IllegalArgumentException("invalid $sortOrder")
        }
        return album.copy(songs = songs)
    }

    private fun getSongLoaderSortOrder(): String {
        return PreferenceUtil.albumSortOrder + ", " +
                PreferenceUtil.albumSongSortOrder
    }


}
