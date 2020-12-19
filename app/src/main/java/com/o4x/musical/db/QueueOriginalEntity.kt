package com.o4x.musical.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_songs_original")
data class QueueOriginalEntity(
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    @ColumnInfo(name = "id") var id: Long
)