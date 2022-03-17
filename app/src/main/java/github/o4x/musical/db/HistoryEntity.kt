package github.o4x.musical.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class HistoryEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "time_played")
    val timePlayed: Long
)