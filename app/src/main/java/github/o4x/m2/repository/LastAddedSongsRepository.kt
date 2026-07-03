package github.o4x.m2.repository

import android.database.Cursor
import android.provider.MediaStore
import github.o4x.m2.model.Album
import github.o4x.m2.model.Artist
import github.o4x.m2.model.Song
import github.o4x.m2.prefs.PreferenceUtil.smartPlaylistLimit

class LastAddedRepository(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository
) {

    private fun getAllRecentSongs(): List<Song> {
        return songRepository.songs(makeLastAddedCursor())
    }

    fun recentSongs(): List<Song> {
        // Use Bundle LIMIT to fetch only the N newest songs instead of all songs
        return songRepository.songs(songRepository.makeLastAddedSongsCursor(smartPlaylistLimit))
    }

    private fun getAllRecentAlbums(): List<Album> {
        return albumRepository.splitIntoAlbums(getAllRecentSongs())
    }

    fun recentAlbums(): List<Album> {
        return getAllRecentAlbums().take(smartPlaylistLimit)
    }

    private fun getAllRecentArtist(): List<Artist> {
        return artistRepository.splitIntoArtists(recentAlbums())
    }

    fun recentArtists(): List<Artist> {
        return getAllRecentArtist().take(smartPlaylistLimit)
    }

    private fun makeLastAddedCursor(): Cursor? {
        return songRepository.makeSongCursor(
            null,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )
    }
}
