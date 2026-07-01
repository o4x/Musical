package github.o4x.musical.db

import androidx.room.*

@Dao
interface QueueOriginalDao {
    @Query("SELECT * FROM QueueOriginalEntity")
    suspend fun getQueueSongsSync(): List<QueueOriginalEntity>

    @Query("DELETE from QueueOriginalEntity")
    suspend fun clearQueueSongs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSongs(songs: List<QueueOriginalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: QueueOriginalEntity)

    @Delete
    suspend fun delete(song: QueueOriginalEntity)
}
