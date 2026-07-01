package github.o4x.musical.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongInHistory(historyEntity: HistoryEntity)

    @Query("DELETE FROM HistoryEntity WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("SELECT * FROM HistoryEntity WHERE id = :songId LIMIT 1")
    suspend fun isSongPresentInHistory(songId: Long): HistoryEntity?

    @Query("SELECT * FROM HistoryEntity ORDER BY time_played DESC")
    suspend fun historySongs(): List<HistoryEntity>

    @Query("SELECT * FROM HistoryEntity ORDER BY time_played DESC")
    fun observableHistorySongs(): LiveData<List<HistoryEntity>>
}
