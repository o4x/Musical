package com.o4x.musical.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        HistoryEntity::class,
        PlayCountEntity::class,
        QueueEntity::class,
        LyricsEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class MusicalDatabase : RoomDatabase() {
    abstract fun playCountDao(): PlayCountDao
    abstract fun queueDao(): QueueDao
    abstract fun queueOriginalDao(): QueueOriginalDao
    abstract fun historyDao(): HistoryDao
    abstract fun lyricsDao(): LyricsDao
}