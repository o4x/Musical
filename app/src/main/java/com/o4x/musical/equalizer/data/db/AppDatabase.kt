package com.o4x.musical.equalizer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.o4x.musical.equalizer.data.model.db.EqualizerPresetEntity


@Database(
    entities = [
        EqualizerPresetEntity::class
    ], version = 1, exportSchema = true
)
@TypeConverters(CustomTypeConverters::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun equalizerPresetsDao(): EqualizerPresetsDao
}