package github.o4x.musical.service.misc

import android.os.Handler
import github.o4x.musical.service.MusicService
import java.lang.ref.WeakReference

class ThrottledSeekHandler(service: MusicService, private val mHandler: Handler) : Runnable {

    private val serviceRef = WeakReference(service)

    companion object {
        // milliseconds to throttle before calling run() to aggregate events
        private const val THROTTLE: Long = 500
    }

    fun notifySeek() {
        serviceRef.get()?.updateMediaSessionPlaybackState()
        mHandler.removeCallbacks(this)
        mHandler.postDelayed(this, THROTTLE)
    }

    override fun run() {
        val service = serviceRef.get() ?: return
        service.savePositionInTrack()
        service.sendPublicIntent(MusicService.PLAY_STATE_CHANGED) // for musixmatch synced lyrics
    }
}
