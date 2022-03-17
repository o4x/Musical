package github.o4x.musical.helper

import android.view.View
import github.o4x.musical.helper.MusicPlayerRemote.isPlaying
import github.o4x.musical.helper.MusicPlayerRemote.pauseSong
import github.o4x.musical.helper.MusicPlayerRemote.resumePlaying

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