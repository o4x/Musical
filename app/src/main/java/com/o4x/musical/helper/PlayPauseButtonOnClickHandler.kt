package com.o4x.musical.helper

import android.view.View
import com.o4x.musical.helper.MusicPlayerRemote.isPlaying
import com.o4x.musical.helper.MusicPlayerRemote.pauseSong
import com.o4x.musical.helper.MusicPlayerRemote.resumePlaying

class PlayPauseButtonOnClickHandler : View.OnClickListener {
    override fun onClick(v: View) {
        playPause()
    }

    companion object {
        @JvmStatic
        fun playPause() {
            if (isPlaying) {
                pauseSong()
            } else {
                resumePlaying()
            }
        }
    }
}