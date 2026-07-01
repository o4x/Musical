package github.o4x.musical.db

import androidx.room.*

@Dao
interface LyricsDao {
    @Query("SELECT * FROM LyricsEntity WHERE songId =:songId LIMIT 1")
    suspend fun lyricsWithSongId(songId: Int): LyricsEntity?

    @Insert
    suspend fun insertLyrics(lyricsEntity: LyricsEntity)

    @Delete
    suspend fun deleteLyrics(lyricsEntity: LyricsEntity)

    @Update
    suspend fun updateLyrics(lyricsEntity: LyricsEntity)
}
