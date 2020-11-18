package com.o4x.musical.service.misc

import com.o4x.musical.helper.StopWatch
import com.o4x.musical.model.Song

class SongPlayCountHelper {
    companion object {
        val TAG: String = SongPlayCountHelper::class.java.simpleName
    }

    private val stopWatch = StopWatch()

    var song = Song.emptySong
        private set

    fun shouldBumpPlayCount(): Boolean {
        return song.duration * 0.5 < stopWatch.elapsedTime
    }

    fun notifySongChanged(song: Song) {
        synchronized(this) {
            stopWatch.reset()
            this.song = song
        }
    }

    fun notifyPlayStateChanged(isPlaying: Boolean) {
        synchronized(this) {
            if (isPlaying) {
                stopWatch.start()
            } else {
                stopWatch.pause()
            }
        }
    }
}