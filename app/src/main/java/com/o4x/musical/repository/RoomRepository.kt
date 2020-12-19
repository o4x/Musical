package com.o4x.musical.repository

import android.database.Cursor
import android.provider.BaseColumns
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.o4x.musical.db.*
import com.o4x.musical.db.toHistoryEntity
import com.o4x.musical.model.Song

class RoomRepository(
    private val songRepository: SongRepository,
    private val playlistDao: PlaylistDao,
    private val playCountDao: PlayCountDao,
    private val queueDao: QueueDao,
    private val queueOriginalDao: QueueOriginalDao,
    private val historyDao: HistoryDao,
    private val lyricsDao: LyricsDao
) {
    @WorkerThread
    suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long =
        playlistDao.createPlaylist(playlistEntity)

    @WorkerThread
    suspend fun checkPlaylistExists(playlistName: String): List<PlaylistEntity> =
        playlistDao.isPlaylistExists(playlistName)

    @WorkerThread
    suspend fun playlists(): List<PlaylistEntity> = playlistDao.playlists()

    @WorkerThread
    suspend fun playlistWithSongs(): List<PlaylistWithSongs> =
        playlistDao.playlistsWithSongs()

    @WorkerThread
    suspend fun insertSongs(songs: List<SongEntity>) {
        playlistDao.insertSongsToPlaylist(songs)
    }


    fun getSongs(playlistEntity: PlaylistEntity): LiveData<List<SongEntity>> =
        playlistDao.songsFromPlaylist(playlistEntity.playListId)

    suspend fun deletePlaylistEntities(playlistEntities: List<PlaylistEntity>) =
        playlistDao.deletePlaylists(playlistEntities)

    suspend fun renamePlaylistEntity(playlistId: Long, name: String) =
        playlistDao.renamePlaylist(playlistId, name)

    suspend fun deleteSongsInPlaylist(songs: List<SongEntity>) {
        songs.forEach {
            playlistDao.deleteSongFromPlaylist(it.playlistCreatorId, it.id)
        }
    }

    suspend fun deletePlaylistSongs(playlists: List<PlaylistEntity>) =
        playlists.forEach {
            playlistDao.deletePlaylistSongs(it.playListId)
        }

    suspend fun removeSongFromPlaylist(songEntity: SongEntity) =
        playlistDao.deleteSongFromPlaylist(songEntity.playlistCreatorId, songEntity.id)

    suspend fun addSongToHistory(currentSong: Song) =
        historyDao.insertSongInHistory(currentSong.toHistoryEntity(System.currentTimeMillis()))

    suspend fun songPresentInHistory(song: Song): HistoryEntity? =
        historyDao.isSongPresentInHistory(song.id)

    suspend fun updateHistorySong(song: Song) =
        historyDao.updateHistorySong(song.toHistoryEntity(System.currentTimeMillis()))

    fun observableHistorySongs(): LiveData<List<HistoryEntity>> =
        historyDao.observableHistorySongs()

    fun historySongs(): List<HistoryEntity> = historyDao.historySongs()

    suspend fun insertSongInPlayCount(playCountEntity: PlayCountEntity) =
        playCountDao.insertSongInPlayCount(playCountEntity)

    suspend fun updateSongInPlayCount(playCountEntity: PlayCountEntity) =
        playCountDao.updateSongInPlayCount(playCountEntity)

    suspend fun deleteSongInPlayCount(playCountEntity: PlayCountEntity) =
        playCountDao.deleteSongInPlayCount(playCountEntity)

    suspend fun checkSongExistInPlayCount(songId: Long): List<PlayCountEntity> =
        playCountDao.checkSongExistInPlayCount(songId)

    suspend fun playCountSongs(): List<PlayCountEntity> =
        playCountDao.playCountSongs()

    suspend fun deleteSongs(songs: List<Song>) {
        songs.forEach {
            playCountDao.deleteSong(it.id)
        }
    }

    fun savedPlayingQueue(): List<Song> {
        return songRepository.songs(queueDao.getQueueSongsSync().toIds().toLongArray())
    }

    fun saveQueue(songs: List<Song>) {
        queueDao.clearQueueSongs()
        queueDao.insertAllSongs(songs.toQueuesEntity())
    }

    fun savedOriginalPlayingQueue(): List<Song> {
        return songRepository.songs(queueOriginalDao.getQueueSongsSync().toIds().toLongArray())
    }

    fun saveOriginalQueue(songs: List<Song>) {
        queueOriginalDao.clearQueueSongs()
        queueOriginalDao.insertAllSongs(songs.toQueuesEntity())
    }
}