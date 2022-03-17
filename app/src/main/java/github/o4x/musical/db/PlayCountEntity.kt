package github.o4x.musical.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class PlayCountEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "play_count")
    var playCount: Int
)