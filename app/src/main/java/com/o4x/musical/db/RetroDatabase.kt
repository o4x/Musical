package com.o4x.musical.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlaylistEntity::class,
        SongEntity::class,
        HistoryEntity::class,
        PlayCountEntity::class,
        QueueEntity::class,
        QueueOriginalEntity::class,
        LyricsEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class RetroDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun playCountDao(): PlayCountDao
    abstract fun queueDao(): QueueDao
    abstract fun queueOriginalDao(): QueueOriginalDao
    abstract fun historyDao(): HistoryDao
    abstract fun lyricsDao(): LyricsDao
}