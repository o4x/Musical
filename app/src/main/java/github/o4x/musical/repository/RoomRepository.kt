package github.o4x.musical.repository

import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import github.o4x.musical.db.*
import github.o4x.musical.model.Song
import github.o4x.musical.prefs.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RoomRepository(
    private val songRepository: SongRepository,
    private val historyDao: HistoryDao,
    private val playCountDao: PlayCountDao,
    private val queueDao: QueueDao,
    private val queueOriginalDao: QueueOriginalDao,
    private val lyricsDao: LyricsDao
) {
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private suspend fun deleteMissingIds(ids: List<Long>) {
        historyDao.delete(ids)
        playCountDao.delete(ids)
    }

    private suspend fun getSortedSongAndClearUpDatabase(ids: List<Long>): List<Song> {
        val selection = songRepository.makeSelection(ids.toLongArray())
        val cursor = songRepository.makeSongCursor(selection, null)
            ?: return songRepository.songs(null)

        val sortedLongCursor = SortedLongCursor(
            cursor, ids.toLongArray(), MediaStore.Audio.AudioColumns._ID)
        deleteMissingIds(sortedLongCursor.missingIds)
        return songRepository.songs(sortedLongCursor)
    }

    fun observableHistorySongs(): LiveData<List<Song>> =
        historyDao.observableHistorySongs().switchMap { entities ->
            liveData(IO) {
                emit(
                    getSortedSongAndClearUpDatabase(entities.historyToIds())
                        .take(PreferenceUtil.smartPlaylistLimit)
                )
            }
        }

    suspend fun historySongs(): List<Song> {
        val limit = PreferenceUtil.smartPlaylistLimit
        // Pre-limit IDs so the MediaStore IN clause stays small
        val ids = historyDao.historySongs().historyToIds().take(limit)
        return getSortedSongAndClearUpDatabase(ids)
    }

    suspend fun addPlayCount(song: Song) {
        historyDao.insertSongInHistory(song.toHistoryEntity(System.currentTimeMillis()))
        val l = playCountDao.checkSongExistInPlayCount(song.id)
        if (l.isNotEmpty()) {
            playCountDao.updateQuantity(song.id)
        } else {
            playCountDao.insertSongInPlayCount(song.toPlayCount())
        }
    }

    suspend fun playCountSongs(): List<Song> =
        getSortedSongAndClearUpDatabase(playCountDao.playCountSongs().playCountToIds())
            .take(PreferenceUtil.smartPlaylistLimit)

    suspend fun notRecentlyPlayedTracks(): List<Song> {
        val allSongs = songRepository.songs().toMutableList()
        val playedSongs = playCountSongs()
        allSongs.removeAll(playedSongs)
        return allSongs.take(PreferenceUtil.smartPlaylistLimit)
    }

    suspend fun savedPlayingQueue(): List<Song> {
        return getSortedSongAndClearUpDatabase(queueDao.getQueueSongsSync().queueToIds())
    }

    suspend fun saveQueue(songs: List<Song>) {
        queueDao.clearQueueSongs()
        queueDao.insertAllSongs(songs.toQueuesEntity())
    }

    suspend fun savedOriginalPlayingQueue(): List<Song> {
        return getSortedSongAndClearUpDatabase(queueOriginalDao.getQueueSongsSync().queueOriginalToIds())
    }

    suspend fun saveOriginalQueue(songs: List<Song>) {
        queueOriginalDao.clearQueueSongs()
        queueOriginalDao.insertAllSongs(songs.toQueuesOriginalEntity())
    }
}
