package com.o4x.musical.repository

import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.o4x.musical.db.*
import com.o4x.musical.model.Song
import com.o4x.musical.prefs.PreferenceUtil
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RoomRepository(
    private val songRepository: SongRepository,
    private val historyDao: HistoryDao,
    private val playCountDao: PlayCountDao,
    private val queueDao: QueueDao,
    private val queueOriginalDao: QueueOriginalDao,
    private val lyricsDao: LyricsDao
) {

    private fun deleteMissingIds(ids: List<Long>) {
        GlobalScope.launch(IO) {
            historyDao.delete(ids)
            playCountDao.delete(ids)
        }
    }

    private fun getSortedSongAndClearUpDatabase(ids: List<Long>): List<Song> {
        val selection = songRepository.makeSelection(ids.toLongArray())
        val cursor = songRepository.makeSongCursor(selection, null)
            ?: return songRepository.songs(null)

        val sortedLongCursor = SortedLongCursor(
            cursor, ids.toLongArray(), MediaStore.Audio.AudioColumns._ID)
        deleteMissingIds(sortedLongCursor.missingIds)
        return songRepository.songs(sortedLongCursor)
    }

    fun observableHistorySongs(): LiveData<List<Song>> =
        Transformations.map(historyDao.observableHistorySongs()) {
            getSortedSongAndClearUpDatabase(it.historyToIds())
                .take(PreferenceUtil.smartPlaylistLimit)
        }

    fun historySongs(): List<Song> =
        getSortedSongAndClearUpDatabase(historyDao.historySongs().historyToIds())
            .take(PreferenceUtil.smartPlaylistLimit)

    fun addPlayCount(song: Song) {
        historyDao.insertSongInHistory(song.toHistoryEntity(System.currentTimeMillis()))
        val l = playCountDao.checkSongExistInPlayCount(song.id)
        if (l.isNotEmpty()) {
            playCountDao.updateQuantity(song.id)
        } else {
            playCountDao.insertSongInPlayCount(song.toPlayCount())
        }
    }

    fun playCountSongs(): List<Song> =
        getSortedSongAndClearUpDatabase(playCountDao.playCountSongs().playCountToIds())
            .take(PreferenceUtil.smartPlaylistLimit)

    fun notRecentlyPlayedTracks(): List<Song> {
        val allSongs = songRepository.songs().toMutableList()
        val playedSongs = playCountSongs()
        allSongs.removeAll(playedSongs)
        return allSongs.take(PreferenceUtil.smartPlaylistLimit)
    }

    fun savedPlayingQueue(): List<Song> {
        return getSortedSongAndClearUpDatabase(queueDao.getQueueSongsSync().queueToIds())
    }

    fun saveQueue(songs: List<Song>) {
        queueDao.clearQueueSongs()
        queueDao.insertAllSongs(songs.toQueuesEntity())
    }

    fun savedOriginalPlayingQueue(): List<Song> {
        return getSortedSongAndClearUpDatabase(queueOriginalDao.getQueueSongsSync().queueOriginalToIds())
    }

    fun saveOriginalQueue(songs: List<Song>) {
        queueOriginalDao.clearQueueSongs()
        queueOriginalDao.insertAllSongs(songs.toQueuesOriginalEntity())
    }
}