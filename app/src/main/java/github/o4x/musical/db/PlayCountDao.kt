package github.o4x.musical.db

import androidx.room.*

@Dao
interface PlayCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSongInPlayCount(playCountEntity: PlayCountEntity)

    @Update
    fun updateSongInPlayCount(playCountEntity: PlayCountEntity)

    @Query("DELETE FROM PlayCountEntity WHERE id IN (:ids)")
    fun delete(ids: List<Long>)

    @Query("SELECT * FROM PlayCountEntity WHERE id =:songId")
    fun checkSongExistInPlayCount(songId: Long): List<PlayCountEntity>

    @Query("SELECT * FROM PlayCountEntity ORDER BY play_count DESC")
    fun playCountSongs(): List<PlayCountEntity>

    @Query("UPDATE PlayCountEntity SET play_count = play_count + 1 WHERE id = :id")
    fun updateQuantity(id: Long)
}