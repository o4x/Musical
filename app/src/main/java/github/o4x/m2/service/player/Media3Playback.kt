package github.o4x.m2.service.player

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.widget.Toast
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import github.o4x.m2.R
import github.o4x.m2.model.Song
import github.o4x.m2.prefs.PreferenceUtil
import github.o4x.m2.service.playback.Playback
import github.o4x.m2.util.MusicUtil

/**
 * ExoPlayer-based playback engine. The player only ever holds the current
 * track plus, when gapless playback is enabled, the next one as a second
 * playlist item so ExoPlayer can transition between them seamlessly.
 * ExoPlayer also owns audio focus (including ducking), pausing on
 * becoming-noisy, and the playback wake lock.
 *
 * Must only be accessed from the main thread.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class Media3Playback(private val context: Context) : Playback, Player.Listener {

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus = */ true
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .build()

    private var callbacks: Playback.PlaybackCallbacks? = null

    override var isInitialized = false
        private set

    init {
        player.addListener(this)
        openAudioEffectSession()
    }

    override val isPlaying: Boolean
        get() = isInitialized && player.isPlaying

    override val audioSessionId: Int
        get() = player.audioSessionId

    override fun setDataSource(song: Song): Boolean {
        isInitialized = false
        if (song.id == -1L) {
            // Match the old MediaPlayer reset() semantics: an unplayable
            // (empty) song stops whatever is currently playing.
            stop()
            return false
        }
        player.setMediaItem(song.toMediaItem())
        player.prepare()
        isInitialized = true
        return true
    }

    override fun setNextDataSource(song: Song?) {
        // Trim the playlist down to just the current item…
        while (player.currentMediaItemIndex > 0) {
            player.removeMediaItem(0)
        }
        while (player.mediaItemCount > 1) {
            player.removeMediaItem(player.mediaItemCount - 1)
        }
        // …then append the next track so ExoPlayer can play it gaplessly.
        if (song != null && song.id != -1L && PreferenceUtil.gaplessPlayback()) {
            player.addMediaItem(song.toMediaItem())
        }
    }

    override fun setCallbacks(callbacks: Playback.PlaybackCallbacks) {
        this.callbacks = callbacks
    }

    override fun start(): Boolean {
        player.play()
        return true
    }

    override fun stop() {
        player.stop()
        player.clearMediaItems()
        isInitialized = false
    }

    override fun release() {
        stop()
        player.release()
    }

    override fun pause(): Boolean {
        player.pause()
        return true
    }

    override fun duration(): Int {
        if (!isInitialized) return -1
        val duration = player.duration
        return if (duration == C.TIME_UNSET) -1 else duration.toInt()
    }

    override fun position(): Int {
        if (!isInitialized) return -1
        return player.currentPosition.toInt()
    }

    override fun seek(whereto: Int): Int {
        player.seekTo(whereto.toLong())
        return whereto
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            callbacks?.onTrackWentToNext()
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) {
            callbacks?.onTrackEnded()
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        callbacks?.onPlayStateChanged()
    }

    override fun onPlayerError(error: PlaybackException) {
        isInitialized = false
        Toast.makeText(context, context.getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show()
        // Mirror the old MediaPlayer behavior where onError fell through to the
        // completion callback: the service either advances or stops cleanly.
        callbacks?.onTrackEnded()
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumName)
            .apply {
                if (PreferenceUtil.albumArtOnLockscreen()) {
                    setArtworkUri(MusicUtil.getMediaStoreAlbumCoverUri(albumId))
                }
            }
            .build()
        return MediaItem.Builder()
            .setUri(MusicUtil.getFileUriFromSong(id))
            .setMediaId(id.toString())
            .setMediaMetadata(metadata)
            .build()
    }

    private fun openAudioEffectSession() {
        val intent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        }
        context.sendBroadcast(intent)
    }
}
