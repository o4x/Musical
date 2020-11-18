package com.o4x.musical.service.misc

import android.os.Handler
import com.o4x.musical.service.MusicService

class ThrottledSeekHandler(private val service: MusicService, private val mHandler: Handler)
    : Runnable {

    companion object {
        // milliseconds to throttle before calling run() to aggregate events
        private const val THROTTLE: Long = 500
    }

    fun notifySeek() {
        service.updateMediaSessionPlaybackState()
        mHandler.removeCallbacks(this)
        mHandler.postDelayed(this, THROTTLE)
    }

    override fun run() {
        service.savePositionInTrack()
        service.sendPublicIntent(MusicService.PLAY_STATE_CHANGED) // for musixmatch synced lyrics
    }
}