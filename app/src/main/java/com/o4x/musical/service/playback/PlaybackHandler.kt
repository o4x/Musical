package com.o4x.musical.service.playback

import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.o4x.musical.service.MusicService
import com.o4x.musical.prefs.PreferenceUtil
import java.lang.ref.WeakReference

class PlaybackHandler(service: MusicService, looper: Looper) : Handler(looper) {

    private val mService: WeakReference<MusicService> = WeakReference(service)
    private var currentDuckVolume = 1.0f

    override fun handleMessage(msg: Message) {
        val service = mService.get() ?: return
        when (msg.what) {
            MusicService.DUCK -> {
                if (PreferenceUtil.audioDucking()) {
                    currentDuckVolume -= .05f
                    if (currentDuckVolume > .2f) {
                        sendEmptyMessageDelayed(MusicService.DUCK, 10)
                    } else {
                        currentDuckVolume = .2f
                    }
                } else {
                    currentDuckVolume = 1f
                }
                service.playback.setVolume(currentDuckVolume)
            }
            MusicService.UNDUCK -> {
                if (PreferenceUtil.audioDucking()) {
                    currentDuckVolume += .03f
                    if (currentDuckVolume < 1f) {
                        sendEmptyMessageDelayed(MusicService.UNDUCK, 10)
                    } else {
                        currentDuckVolume = 1f
                    }
                } else {
                    currentDuckVolume = 1f
                }
                service.playback.setVolume(currentDuckVolume)
            }
            MusicService.TRACK_WENT_TO_NEXT -> if (service.pendingQuit || service.repeatMode == MusicService.REPEAT_MODE_NONE && service.isLastTrack) {
                service.pause()
                service.seek(0)
                if (service.pendingQuit) {
                    service.pendingQuit = false
                    service.quit()
                    return
                }
            } else {
                service.position = service.nextPosition
                service.prepareNextImpl()
                service.notifyChange(MusicService.META_CHANGED)
            }
            MusicService.TRACK_ENDED -> {
                // if there is a timer finished, don't continue
                if (service.pendingQuit ||
                    service.repeatMode == MusicService.REPEAT_MODE_NONE && service.isLastTrack
                ) {
                    service.notifyChange(MusicService.PLAY_STATE_CHANGED)
                    service.seek(0)
                    if (service.pendingQuit) {
                        service.pendingQuit = false
                        service.quit()
                        return
                    }
                } else {
                    service.playNextSong(false)
                }
                sendEmptyMessage(MusicService.RELEASE_WAKELOCK)
            }
            MusicService.RELEASE_WAKELOCK -> service.releaseWakeLock()
            MusicService.PLAY_SONG -> service.playSongAtImpl(msg.arg1)
            MusicService.SET_POSITION -> {
                val playing = service.isPlaying
                if (service.openTrackAndPrepareNextAt(msg.arg1) && playing) {
                    service.play()
                }
            }
            MusicService.PREPARE_NEXT -> service.prepareNextImpl()
            MusicService.RESTORE_QUEUES -> service.restoreQueuesAndPositionIfNecessary()
            MusicService.FOCUS_CHANGE -> when (msg.arg1) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (!service.isPlaying && service.pausedByTransientLossOfFocus) {
                        service.play()
                        service.pausedByTransientLossOfFocus = false
                    }
                    removeMessages(MusicService.DUCK)
                    sendEmptyMessage(MusicService.UNDUCK)
                }
                AudioManager.AUDIOFOCUS_LOSS ->                             // Lost focus for an unbounded amount of time: stop playback and release media playback
                    service.pause()
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Lost focus for a short time, but we have to stop
                    // playback. We don't release the media playback because playback
                    // is likely to resume
                    val wasPlaying = service.isPlaying
                    service.pause()
                    service.pausedByTransientLossOfFocus = wasPlaying
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // Lost focus for a short time, but it's ok to keep playing
                    // at an attenuated level
                    removeMessages(MusicService.UNDUCK)
                    sendEmptyMessage(MusicService.DUCK)
                }
            }
        }
    }

}