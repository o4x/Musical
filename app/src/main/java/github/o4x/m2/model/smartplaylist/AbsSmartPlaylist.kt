package github.o4x.m2.model.smartplaylist

import androidx.annotation.DrawableRes
import github.o4x.m2.R
import github.o4x.m2.model.AbsCustomPlaylist

abstract class AbsSmartPlaylist(
    name: String,
    @DrawableRes val iconRes: Int = R.drawable.ic_queue_music
) : AbsCustomPlaylist(
    id = PlaylistIdGenerator(name, iconRes),
    name = name
)