package com.o4x.musical.ui.fragments.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.o4x.musical.R
import com.o4x.musical.service.MusicService

@BindingAdapter("repeatMode")
fun setRepeatMode(view: ImageView, mode: Int) {
    when (mode) {
        MusicService.REPEAT_MODE_ALL -> view.setImageResource(R.drawable.ic_repeat)
        MusicService.REPEAT_MODE_THIS -> view.setImageResource(R.drawable.ic_repeat_one)
        else -> view.setImageResource(R.drawable.ic_repeat)
    }
}