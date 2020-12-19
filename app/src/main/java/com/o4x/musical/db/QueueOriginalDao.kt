package com.o4x.musical.db

import androidx.room.*

@Dao
interface QueueOriginalDao {
    @Query("SELECT * FROM queue_songs")
    fun getQueueSongsSync(): List<QueueEntity>

    @Query("DELETE from queue_songs")
    fun clearQueueSongs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSongs(songs: List<QueueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSong(song: QueueEntity)

    @Delete
    fun delete(song: QueueEntity)
}