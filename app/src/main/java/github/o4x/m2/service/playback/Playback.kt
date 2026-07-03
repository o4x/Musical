package github.o4x.m2.service.playback

import github.o4x.m2.model.Song

interface Playback {

    val isInitialized: Boolean

    val isPlaying: Boolean

    val audioSessionId: Int

    fun setDataSource(song: Song): Boolean

    fun setNextDataSource(song: Song?)

    fun setCallbacks(callbacks: PlaybackCallbacks)

    fun start(): Boolean

    fun stop()

    fun release()

    fun pause(): Boolean

    fun duration(): Int

    fun position(): Int

    fun seek(whereto: Int): Int

    interface PlaybackCallbacks {
        fun onTrackWentToNext()

        fun onTrackEnded()

        fun onPlayStateChanged()
    }
}
