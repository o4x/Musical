package github.o4x.musical.db

import androidx.room.*

@Dao
interface QueueOriginalDao {
    @Query("SELECT * FROM QueueOriginalEntity")
    fun getQueueSongsSync(): List<QueueOriginalEntity>

    @Query("DELETE from QueueOriginalEntity")
    fun clearQueueSongs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSongs(songs: List<QueueOriginalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSong(song: QueueOriginalEntity)

    @Delete
    fun delete(song: QueueOriginalEntity)
}