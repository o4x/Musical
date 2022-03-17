package github.o4x.musical.model.smartplaylist

import androidx.annotation.DrawableRes
import github.o4x.musical.R
import github.o4x.musical.model.AbsCustomPlaylist

abstract class AbsSmartPlaylist(
    name: String,
    @DrawableRes val iconRes: Int = R.drawable.ic_queue_music
) : AbsCustomPlaylist(
    id = PlaylistIdGenerator(name, iconRes),
    name = name
)