package com.o4x.musical.db

import androidx.room.*

@Dao
interface QueueOriginalDao {
    @Query("SELECT * FROM queue_songs_original")
    fun getQueueSongsSync(): List<QueueOriginalEntity>

    @Query("DELETE from queue_songs_original")
    fun clearQueueSongs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSongs(songs: List<QueueOriginalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSong(song: QueueOriginalEntity)

    @Delete
    fun delete(song: QueueOriginalEntity)
}