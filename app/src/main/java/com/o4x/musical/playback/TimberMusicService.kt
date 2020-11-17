package com.o4x.musical.playback

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.Nullable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.o4x.musical.R
import com.o4x.musical.db.QueueEntity
import com.o4x.musical.db.QueueHelper
import com.o4x.musical.extensions.*
import com.o4x.musical.model.MediaID
import com.o4x.musical.model.MediaID.Companion.CALLER_OTHER
import com.o4x.musical.model.MediaID.Companion.CALLER_SELF
import com.o4x.musical.notifications.Notifications
import com.o4x.musical.permissions.PermissionsManager
import com.o4x.musical.playback.players.MediaSessionCallback.Companion.ACTION_NEXT
import com.o4x.musical.playback.players.MediaSessionCallback.Companion.ACTION_PLAY_PAUSE
import com.o4x.musical.playback.players.MediaSessionCallback.Companion.ACTION_PREVIOUS
import com.o4x.musical.playback.players.MediaSessionCallback.Companion.APP_PACKAGE_NAME
import com.o4x.musical.playback.players.SongPlayer
import io.reactivex.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import timber.log.Timber

// TODO pull out media logic to separate class to make this more readable
class TimberMusicService : MediaBrowserServiceCompat(), KoinComponent, LifecycleOwner {

    companion object {
        const val MEDIA_ID_ARG = "MEDIA_ID"
        const val MEDIA_TYPE_ARG = "MEDIA_TYPE"
        const val MEDIA_CALLER = "MEDIA_CALLER"
        const val MEDIA_ID_ROOT = -1
        const val TYPE_ALL_ARTISTS = 0
        const val TYPE_ALL_ALBUMS = 1
        const val TYPE_ALL_SONGS = 2
        const val TYPE_ALL_PLAYLISTS = 3
        const val TYPE_SONG = 9
        const val TYPE_ALBUM = 10
        const val TYPE_ARTIST = 11
        const val TYPE_PLAYLIST = 12
        const val TYPE_ALL_FOLDERS = 13
        const val TYPE_ALL_GENRES = 14
        const val TYPE_GENRE = 15

        const val NOTIFICATION_ID = 888
    }

    private val notifications by inject<Notifications>()

    private lateinit var player: SongPlayer
    private val queueHelper by inject<QueueHelper>()
    private val permissionsManager by inject<PermissionsManager>()

    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private val lifecycle = LifecycleRegistry(this)

    override fun getLifecycle() = lifecycle

    override fun onCreate() {
        super.onCreate()
        lifecycle.currentState = Lifecycle.State.RESUMED
        Timber.d("onCreate()")

        // We get it here so we don't end up lazy-initializing it from a non-UI thread.
        player = get()

        // We wait until the permission is granted to set the initial queue.
        // This observable will immediately emit if the permission is already granted.
        permissionsManager.requestStoragePermission(waitForGranted = true)
            .subscribe(Consumer {
                GlobalScope.launch(Dispatchers.IO) {
                    player.setQueue()
                }
            })
            .attachLifecycle(this)

        sessionToken = player.getSession().sessionToken
        becomingNoisyReceiver = BecomingNoisyReceiver(this, sessionToken!!)

        player.onPlayingState { isPlaying ->
            if (isPlaying) {
                becomingNoisyReceiver.register()
                startForeground(NOTIFICATION_ID, notifications.buildNotification(getSession()))
            } else {
                becomingNoisyReceiver.unregister()
                stopForeground(false)
                saveCurrentData()
            }
            notifications.updateNotification(player.getSession())
        }

        player.onCompletion {
            notifications.updateNotification(player.getSession())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand(): ${intent?.action}")
        if (intent == null) {
            return START_STICKY
        }

        val mediaSession = player.getSession()
        val controller = mediaSession.controller

        when (intent.action) {
            ACTION_PLAY_PAUSE -> {
                controller.playbackState?.let { playbackState ->
                    when {
                        playbackState.isPlaying -> controller.transportControls.pause()
                        playbackState.isPlayEnabled -> controller.transportControls.play()
                    }
                }
            }
            ACTION_NEXT -> {
                controller.transportControls.skipToNext()
            }
            ACTION_PREVIOUS -> {
                controller.transportControls.skipToPrevious()
            }
        }

        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        lifecycle.currentState = Lifecycle.State.DESTROYED
        Timber.d("onDestroy()")
        saveCurrentData()
        player.release()
        super.onDestroy()
    }

    //media browser
    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        result.detach()

        // Wait to load media item children until we have the storage permission, this prevents crashes
        // and allows us to automatically finish loading once the permission is granted by the user.
        permissionsManager.requestStoragePermission(waitForGranted = true)
            .subscribe(Consumer {
                GlobalScope.launch(Dispatchers.Main) {
                    val mediaItems = withContext(Dispatchers.IO) {
                        loadChildren(parentId)
                    }
                    result.sendResult(mediaItems)
                }
            })
            .attachLifecycle(this)
    }

    @Nullable
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        val caller = if (clientPackageName == APP_PACKAGE_NAME) {
            CALLER_SELF
        } else {
            CALLER_OTHER
        }
        return MediaBrowserServiceCompat.BrowserRoot(MediaID(MEDIA_ID_ROOT.toString(), null, caller).asString(), null)
    }

    private fun loadChildren(parentId: String): ArrayList<MediaBrowserCompat.MediaItem> {
        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
        val mediaIdParent = MediaID().fromString(parentId)

        val mediaType = mediaIdParent.type
        val mediaId = mediaIdParent.mediaId
        val caller = mediaIdParent.caller

        return if (caller == CALLER_SELF) {
            mediaItems
        } else {
            mediaItems.toRawMediaItems()
        }
    }

    private fun saveCurrentData() {
        GlobalScope.launch(Dispatchers.IO) {
            val mediaSession = player.getSession()
            val controller = mediaSession.controller
            if (controller == null ||
                controller.playbackState == null ||
                controller.playbackState.state == PlaybackStateCompat.STATE_NONE
            ) {
                return@launch
            }

            val queue = controller.queue
            val currentId = controller.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            queueHelper.updateQueueSongs(queue?.toIDList(), currentId?.toLong())

            val queueEntity = QueueEntity().apply {
                this.currentId = currentId?.toLong()
                currentSeekPos = controller.playbackState?.position
                repeatMode = controller.repeatMode
                shuffleMode = controller.shuffleMode
                playState = controller.playbackState?.state
                queueTitle = controller.queueTitle?.toString() ?: getString(R.string.all_songs)
            }
            queueHelper.updateQueueData(queueEntity)
        }
    }

}