package github.o4x.m2.db

import androidx.room.*

@Dao
interface PlayCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongInPlayCount(playCountEntity: PlayCountEntity)

    @Update
    suspend fun updateSongInPlayCount(playCountEntity: PlayCountEntity)

    @Query("DELETE FROM PlayCountEntity WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("SELECT * FROM PlayCountEntity WHERE id =:songId")
    suspend fun checkSongExistInPlayCount(songId: Long): List<PlayCountEntity>

    @Query("SELECT * FROM PlayCountEntity ORDER BY play_count DESC")
    suspend fun playCountSongs(): List<PlayCountEntity>

    @Query("UPDATE PlayCountEntity SET play_count = play_count + 1 WHERE id = :id")
    suspend fun updateQuantity(id: Long)
}
