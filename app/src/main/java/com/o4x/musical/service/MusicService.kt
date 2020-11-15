package com.o4x.musical.service

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.database.ContentObserver
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.os.*
import androidx.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import com.o4x.musical.App.Companion.getContext
import com.o4x.musical.R
import com.o4x.musical.appwidgets.AppWidgetBig
import com.o4x.musical.appwidgets.AppWidgetCard
import com.o4x.musical.appwidgets.AppWidgetClassic
import com.o4x.musical.appwidgets.AppWidgetSmall
import com.o4x.musical.helper.CountDownTimerPausable
import com.o4x.musical.helper.ShuffleHelper
import com.o4x.musical.helper.StopWatch
import com.o4x.musical.imageloader.glide.loader.GlideLoader.Companion.with
import com.o4x.musical.imageloader.glide.targets.CustomBitmapTarget
import com.o4x.musical.model.Playlist
import com.o4x.musical.model.Song
import com.o4x.musical.model.Song.Companion.emptySong
import com.o4x.musical.provider.HistoryStore
import com.o4x.musical.provider.QueueStore.Companion.getInstance
import com.o4x.musical.provider.SongPlayCountStore
import com.o4x.musical.service.notification.PlayingNotification
import com.o4x.musical.service.notification.PlayingNotificationImpl
import com.o4x.musical.service.notification.PlayingNotificationImpl24
import com.o4x.musical.service.playback.Playback
import com.o4x.musical.service.playback.Playback.PlaybackCallbacks
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil
import com.o4x.musical.util.PreferenceUtil.albumArtOnLockscreen
import com.o4x.musical.util.PreferenceUtil.audioDucking
import com.o4x.musical.util.PreferenceUtil.isClassicNotification
import com.o4x.musical.util.PreferenceUtil.registerOnSharedPreferenceChangedListener
import com.o4x.musical.util.PreferenceUtil.unregisterOnSharedPreferenceChangedListener
import com.o4x.musical.util.Util
import java.lang.ref.WeakReference
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid), Andrew Neal
 */
class MusicService : Service(), SharedPreferences.OnSharedPreferenceChangeListener,
    PlaybackCallbacks {
    private val musicBind: IBinder = MusicBinder()
    var pendingQuit = false
    private val appWidgetBig = AppWidgetBig.getInstance()
    private val appWidgetClassic = AppWidgetClassic.getInstance()
    private val appWidgetSmall = AppWidgetSmall.getInstance()
    private val appWidgetCard = AppWidgetCard.getInstance()
    private val playback: Playback by lazy { MultiPlayer(this) }
    var playingQueue = mutableListOf<Song>()
    private var originalPlayingQueue = mutableListOf<Song>()
    @JvmField
    var position = -1
    private var nextPosition = -1
    @JvmField
    var shuffleMode = 0
    @JvmField
    var repeatMode = 0
    private var queuesRestored = false
    private var pausedByTransientLossOfFocus = false
    private var playingNotification: PlayingNotification? = null
    private val audioManager: AudioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }
    lateinit var mediaSession: MediaSessionCompat
    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
    }
    private var countDownTimerPausable: CountDownTimerPausable? = null
    private val playerHandler: PlaybackHandler by lazy {
        PlaybackHandler(this, musicPlayerHandlerThread.looper)
    }
    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        playerHandler.obtainMessage(
            FOCUS_CHANGE, focusChange, 0
        ).sendToTarget()
    }
    private val queueSaveHandler: QueueSaveHandler by lazy {
        QueueSaveHandler(this, queueSaveHandlerThread.looper)
    }
    private val musicPlayerHandlerThread: HandlerThread by lazy {
        HandlerThread("PlaybackHandler")
    }
    // queue saving needs to run on a separate thread so that it doesn't block the playback handler events
    private val queueSaveHandlerThread: HandlerThread by lazy {
        HandlerThread("QueueSaveHandler", Process.THREAD_PRIORITY_BACKGROUND)
    }
    private val songPlayCountHelper = SongPlayCountHelper()
    private val throttledSeekHandler: ThrottledSeekHandler by lazy {
        ThrottledSeekHandler(playerHandler)
    }
    private var becomingNoisyReceiverRegistered = false
    private val becomingNoisyReceiverIntentFilter =
        IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }
    private val mediaStoreObserver: ContentObserver by lazy {
        MediaStoreObserver(playerHandler)
    }
    private var notHandledMetaChangedForCurrentTrack = false
    private val uiThreadHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun onCreate() {
        super.onCreate()
        wakeLock.setReferenceCounted(false)
        musicPlayerHandlerThread.start()
        playback.setCallbacks(this)
        setupMediaSession()


        queueSaveHandlerThread.start()
        registerReceiver(widgetIntentReceiver, IntentFilter(APP_WIDGET_UPDATE))
        initNotification()


        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Albums.INTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Artists.INTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Genres.INTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI,
            true,
            mediaStoreObserver
        )
        registerOnSharedPreferenceChangedListener(this)
        restoreState()
        mediaSession.isActive = true
        sendBroadcast(Intent("com.o4x.musical.PHONOGRAPH_MUSIC_SERVICE_CREATED"))
    }

    private fun setupMediaSession() {
        val mediaButtonReceiverComponentName =
            ComponentName(applicationContext, MediaButtonIntentReceiver::class.java)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.component = mediaButtonReceiverComponentName
        val mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, mediaButtonIntent, 0
        )
        mediaSession = MediaSessionCompat(
            this,
            "Musical",
            mediaButtonReceiverComponentName,
            mediaButtonReceiverPendingIntent
        )
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                play()
            }

            override fun onPause() {
                pause()
            }

            override fun onSkipToNext() {
                nextSong(true)
            }

            override fun onSkipToPrevious() {
                back(true)
            }

            override fun onStop() {
                quit()
            }

            override fun onSeekTo(pos: Long) {
                seek(pos.toInt())
            }

            override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                return MediaButtonIntentReceiver.handleIntent(this@MusicService, mediaButtonEvent)
            }
        })
        mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.action != null) {
                restoreQueuesAndPositionIfNecessary()
                when (intent.action) {
                    ACTION_TOGGLE_PAUSE -> if (isPlaying) {
                        pause()
                    } else {
                        play()
                    }
                    ACTION_PAUSE -> pause()
                    ACTION_PLAY -> play()
                    ACTION_PLAY_PLAYLIST -> {
                        val playlist: Playlist? = intent.getParcelableExtra(INTENT_EXTRA_PLAYLIST)
                        val shuffleMode =
                            intent.getIntExtra(INTENT_EXTRA_SHUFFLE_MODE, getShuffleMode())
                        if (playlist != null) {
                            val playlistSongs = playlist.songs()
                            if (playlistSongs.isNotEmpty()) {
                                if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                                    var startPosition = 0
                                    if (playlistSongs.isNotEmpty()) {
                                        startPosition = Random().nextInt(playlistSongs.size)
                                    }
                                    openQueue(playlistSongs, startPosition, true)
                                    setShuffleMode(shuffleMode)
                                } else {
                                    openQueue(playlistSongs, 0, true)
                                }
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    R.string.playlist_is_empty,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                R.string.playlist_is_empty,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    ACTION_REWIND -> back(true)
                    ACTION_SKIP -> nextSong(true)
                    ACTION_STOP, ACTION_QUIT -> {
                        pendingQuit = false
                        quit()
                    }
                    ACTION_PENDING_QUIT -> pendingQuit = true
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(widgetIntentReceiver)
        if (becomingNoisyReceiverRegistered) {
            unregisterReceiver(becomingNoisyReceiver)
            becomingNoisyReceiverRegistered = false
        }
        mediaSession.isActive = false
        quit()
        releaseResources()
        contentResolver.unregisterContentObserver(mediaStoreObserver)
        unregisterOnSharedPreferenceChangedListener(this)
        wakeLock.release()
        sendBroadcast(Intent("com.o4x.musical.PHONOGRAPH_MUSIC_SERVICE_DESTROYED"))
    }

    override fun onBind(intent: Intent): IBinder? {
        return musicBind
    }

    private class QueueSaveHandler(service: MusicService, looper: Looper) : Handler(looper) {
        private val mService: WeakReference<MusicService> = WeakReference(service)

        override fun handleMessage(msg: Message) {
            val service = mService.get()
            when (msg.what) {
                SAVE_QUEUES -> service!!.saveQueuesImpl()
            }
        }
    }

    private fun saveQueuesImpl() {
        getInstance(this).saveQueues(playingQueue, originalPlayingQueue)
    }

    private fun savePosition() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putInt(SAVED_POSITION, getPosition()).apply()
    }

    private fun savePositionInTrack() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putInt(SAVED_POSITION_IN_TRACK, songProgressMillis).apply()
    }

    private fun saveState() {
        saveQueues()
        savePosition()
        savePositionInTrack()
    }

    private fun saveQueues() {
        queueSaveHandler.removeMessages(SAVE_QUEUES)
        queueSaveHandler.sendEmptyMessage(SAVE_QUEUES)
    }

    private fun restoreState() {
        shuffleMode =
            PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_SHUFFLE_MODE, 0)
        repeatMode =
            PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_REPEAT_MODE, 0)
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED)
        handleAndSendChangeInternal(REPEAT_MODE_CHANGED)
        playerHandler.removeMessages(RESTORE_QUEUES)
        playerHandler.sendEmptyMessage(RESTORE_QUEUES)
    }

    @Synchronized
    private fun restoreQueuesAndPositionIfNecessary() {
        if (!queuesRestored && playingQueue.isEmpty()) {
            val restoredQueue = getInstance(this).savedPlayingQueue
            val restoredOriginalQueue = getInstance(this).savedOriginalPlayingQueue
            val restoredPosition = PreferenceManager.getDefaultSharedPreferences(this).getInt(
                SAVED_POSITION, -1
            )
            val restoredPositionInTrack =
                PreferenceManager.getDefaultSharedPreferences(this).getInt(
                    SAVED_POSITION_IN_TRACK, -1
                )
            if (restoredQueue.isNotEmpty() && restoredQueue.size == restoredOriginalQueue.size && restoredPosition != -1) {
                originalPlayingQueue = restoredOriginalQueue.toMutableList()
                playingQueue = restoredQueue.toMutableList()
                position = restoredPosition
                openCurrent()
                prepareNext()
                if (restoredPositionInTrack > 0) seek(restoredPositionInTrack)
                notHandledMetaChangedForCurrentTrack = true
                sendChangeInternal(META_CHANGED)
                sendChangeInternal(QUEUE_CHANGED)
            }
        }
        queuesRestored = true
    }

    private fun quit() {
        pause()
        playingNotification!!.stop()
        closeAudioEffectSession()
        audioManager.abandonAudioFocus(audioFocusListener)
        stopSelf()
    }

    private fun releaseResources() {
        playerHandler.removeCallbacksAndMessages(null)
        musicPlayerHandlerThread.quitSafely()
        queueSaveHandler.removeCallbacksAndMessages(null)
        queueSaveHandlerThread.quitSafely()
        playback.release()
//        playback = null
        mediaSession.release()
    }

    val isPlaying: Boolean
        get() = playback.isPlaying

    private fun getPosition(): Int {
        return position
    }

    fun nextSong(force: Boolean) {
        setPosition(getNextPosition(force))
    }

    fun playNextSong(force: Boolean) {
        playSongAt(getNextPosition(force))
    }

    private fun openTrackAndPrepareNextAt(position: Int): Boolean {
        synchronized(this) {
            this.position = position
            val prepared = openCurrent()
            if (prepared) prepareNextImpl()
            notifyChange(META_CHANGED)
            notHandledMetaChangedForCurrentTrack = false
            return prepared
        }
    }

    private fun openCurrent(): Boolean {
        synchronized(this) {
            return try {
                playback.setDataSource(getTrackUri(currentSong))
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun prepareNext() {
        playerHandler.removeMessages(PREPARE_NEXT)
        playerHandler.obtainMessage(PREPARE_NEXT).sendToTarget()
    }

    private fun prepareNextImpl(): Boolean {
        synchronized(this) {
            return try {
                val nextPosition = getNextPosition(false)
                playback.setNextDataSource(getTrackUri(getSongAt(nextPosition)))
                this.nextPosition = nextPosition
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun closeAudioEffectSession() {
        val audioEffectsIntent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playback.audioSessionId)
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        sendBroadcast(audioEffectsIntent)
    }

    private fun requestFocus(): Boolean {
        return audioManager.requestAudioFocus(
            audioFocusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun initNotification() {
        playingNotification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isClassicNotification) {
                PlayingNotificationImpl24()
            } else {
                PlayingNotificationImpl()
            }
        playingNotification?.init(this)
    }

    private fun updateNotification() {
        if (playingNotification != null && currentSong.id != -1L) {
            playingNotification?.update()
        }
    }

    private fun updateMediaSessionPlaybackState() {
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    songProgressMillis.toLong(), 1f
                )
                .build()
        )
    }

    private fun updateMediaSessionMetaData() {
        val song = currentSong
        if (song.id == -1L) {
            mediaSession.setMetadata(null)
            return
        }
        val metaData = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.albumName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, getPosition() + 1.toLong())
            .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.year.toLong())
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            metaData.putLong(
                MediaMetadataCompat.METADATA_KEY_NUM_TRACKS,
                playingQueue.size.toLong()
            )
        }
        if (albumArtOnLockscreen()) {
            runOnNewThread {
                with(getContext())
                    .load(song).into(object : CustomBitmapTarget(
                        Util.getScreenWidth(),
                        Util.getScreenHeight()
                    ) {
                        override fun setResource(resource: Bitmap) {
                            super.setResource(resource)
                            metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource)
                            mediaSession.setMetadata(metaData.build())
                        }
                    })
            }
        } else {
            mediaSession.setMetadata(metaData.build())
        }
    }

    fun runOnUiThread(runnable: Runnable) {
        uiThreadHandler.post(runnable)
    }

    fun runOnNewThread(runnable: Runnable) {
        Thread(runnable).start()
    }

    val currentSong: Song
        get() = getSongAt(getPosition())

    private fun getSongAt(position: Int): Song {
        return if (position >= 0 && position < playingQueue.size) {
            playingQueue[position]
        } else {
            emptySong
        }
    }

    private fun getNextPosition(force: Boolean): Int {
        var position = getPosition() + 1
        when (getRepeatMode()) {
            REPEAT_MODE_ALL -> if (isLastTrack) {
                position = 0
            }
            REPEAT_MODE_THIS -> if (force) {
                if (isLastTrack) {
                    position = 0
                }
            } else {
                position -= 1
            }
            REPEAT_MODE_NONE -> if (isLastTrack) {
                position -= 1
            }
            else -> if (isLastTrack) {
                position -= 1
            }
        }
        return position
    }

    private val isLastTrack: Boolean
        get() = getPosition() == playingQueue.size - 1

    fun getRepeatMode(): Int {
        return repeatMode
    }

    private fun setRepeatMode(repeatMode: Int) {
        when (repeatMode) {
            REPEAT_MODE_NONE, REPEAT_MODE_ALL, REPEAT_MODE_THIS -> {
                this.repeatMode = repeatMode
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(SAVED_REPEAT_MODE, repeatMode)
                    .apply()
                prepareNext()
                handleAndSendChangeInternal(REPEAT_MODE_CHANGED)
            }
        }
    }

    fun openQueue(playingQueue: List<Song>?, startPosition: Int, startPlaying: Boolean) {
        if (playingQueue != null && playingQueue.isNotEmpty() && startPosition >= 0 && startPosition < playingQueue.size) {
            // it is important to copy the playing queue here first as we might add/remove songs later
            originalPlayingQueue = ArrayList(playingQueue)
            this.playingQueue = ArrayList(originalPlayingQueue)
            var position = startPosition
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                ShuffleHelper.makeShuffleList(this.playingQueue, startPosition)
                position = 0
            }
            if (startPlaying) {
                playSongAt(position)
            } else {
                setPosition(position)
            }
            notifyChange(QUEUE_CHANGED)
        }
    }

    fun addSong(position: Int, song: Song) {
        playingQueue.add(position, song)
        originalPlayingQueue.add(position, song)
        notifyChange(QUEUE_CHANGED)
    }

    fun addSong(song: Song) {
        playingQueue.add(song)
        originalPlayingQueue.add(song)
        notifyChange(QUEUE_CHANGED)
    }

    fun addSongs(position: Int, songs: List<Song>) {
        playingQueue.addAll(position, songs)
        originalPlayingQueue.addAll(position, songs)
        notifyChange(QUEUE_CHANGED)
    }

    fun addSongs(songs: List<Song>) {
        playingQueue.addAll(songs)
        originalPlayingQueue.addAll(songs)
        notifyChange(QUEUE_CHANGED)
    }

    fun removeSong(position: Int) {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            playingQueue.removeAt(position)
            originalPlayingQueue.removeAt(position)
        } else {
            originalPlayingQueue.remove(playingQueue.removeAt(position))
        }
        rePosition(position)
        notifyChange(QUEUE_CHANGED)
    }

    fun removeSong(song: Song) {
        for (i in playingQueue.indices) {
            if (playingQueue[i].id == song.id) {
                playingQueue.removeAt(i)
                rePosition(i)
            }
        }
        for (i in originalPlayingQueue.indices) {
            if (originalPlayingQueue[i].id == song.id) {
                originalPlayingQueue.removeAt(i)
            }
        }
        notifyChange(QUEUE_CHANGED)
    }

    private fun rePosition(deletedPosition: Int) {
        val currentPosition = getPosition()
        if (deletedPosition < currentPosition) {
            position = currentPosition - 1
        } else if (deletedPosition == currentPosition) {
            if (playingQueue.size > deletedPosition) {
                setPosition(position)
            } else {
                setPosition(position - 1)
            }
        }
    }

    fun moveSong(from: Int, to: Int) {
        if (from == to) return
        val currentPosition = getPosition()
        val songToMove: Song = playingQueue.removeAt(from)
        playingQueue.add(to, songToMove)
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            val tmpSong: Song = originalPlayingQueue.removeAt(from)
            originalPlayingQueue.add(to, tmpSong)
        }
        when {
            currentPosition in to until from -> {
                position = currentPosition + 1
            }
            currentPosition in (from + 1)..to -> {
                position = currentPosition - 1
            }
            from == currentPosition -> {
                position = to
            }
        }
        notifyChange(QUEUE_CHANGED)
    }

    fun clearQueue() {
        playingQueue.clear()
        originalPlayingQueue.clear()
        setPosition(-1)
        notifyChange(QUEUE_CHANGED)
    }

    fun playSongAt(position: Int) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(PLAY_SONG)
        playerHandler.obtainMessage(PLAY_SONG, position, 0).sendToTarget()
    }

    private fun setPosition(position: Int) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(SET_POSITION)
        playerHandler.obtainMessage(SET_POSITION, position, 0).sendToTarget()
    }

    private fun playSongAtImpl(position: Int) {
        if (openTrackAndPrepareNextAt(position)) {
            play()
        } else {
            Toast.makeText(this, resources.getString(R.string.unplayable_file), Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun pause() {
        pausedByTransientLossOfFocus = false
        if (playback.isPlaying) {
            playback.pause()
            notifyChange(PLAY_STATE_CHANGED)
        }
    }

    fun play() {
        synchronized(this) {
            if (requestFocus()) {
                if (!playback.isPlaying) {
                    if (!playback.isInitialized) {
                        playSongAt(getPosition())
                    } else {
                        playback.start()
                        if (!becomingNoisyReceiverRegistered) {
                            registerReceiver(
                                becomingNoisyReceiver,
                                becomingNoisyReceiverIntentFilter
                            )
                            becomingNoisyReceiverRegistered = true
                        }
                        if (notHandledMetaChangedForCurrentTrack) {
                            handleChangeInternal(META_CHANGED)
                            notHandledMetaChangedForCurrentTrack = false
                        }
                        notifyChange(PLAY_STATE_CHANGED)

                        // fixes a bug where the volume would stay ducked because the AudioManager.AUDIOFOCUS_GAIN event is not sent
                        playerHandler.removeMessages(DUCK)
                        playerHandler.sendEmptyMessage(UNDUCK)
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.audio_focus_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun playSongs(songs: List<Song>, shuffleMode: Int) {
        if (songs.isNotEmpty()) {
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                var startPosition = 0
                if (songs.isNotEmpty()) {
                    startPosition = Random().nextInt(songs.size)
                }
                openQueue(songs, startPosition, false)
                setShuffleMode(shuffleMode)
            } else {
                openQueue(songs, 0, false)
            }
            play()
        } else {
            Toast.makeText(applicationContext, R.string.playlist_is_empty, Toast.LENGTH_LONG).show()
        }
    }

    fun previousSong(force: Boolean) {
        setPosition(getPreviousPosition(force))
    }

    fun back(force: Boolean) {
        if (songProgressMillis > 5000) {
            seek(0)
        } else {
            previousSong(force)
        }
    }

    private fun getPreviousPosition(force: Boolean): Int {
        var newPosition = getPosition() - 1
        when (repeatMode) {
            REPEAT_MODE_ALL -> if (newPosition < 0) {
                newPosition = playingQueue.size - 1
            }
            REPEAT_MODE_THIS -> if (force) {
                if (newPosition < 0) {
                    newPosition = playingQueue.size - 1
                }
            } else {
                newPosition = getPosition()
            }
            REPEAT_MODE_NONE -> if (newPosition < 0) {
                newPosition = 0
            }
            else -> if (newPosition < 0) {
                newPosition = 0
            }
        }
        return newPosition
    }

    val songProgressMillis: Int
        get() = playback.position()
    val songDurationMillis: Int
        get() = playback.duration()

    fun getQueueDurationMillis(position: Int): Long {
        var duration: Long = 0
        for (i in position + 1 until playingQueue.size) duration += playingQueue[i].duration
        return duration
    }

    fun seek(millis: Int): Int {
        synchronized(this) {
            return try {
                val newPosition = playback.seek(millis)
                throttledSeekHandler.notifySeek()
                newPosition
            } catch (e: Exception) {
                -1
            }
        }
    }

    fun cycleRepeatMode() {
        when (getRepeatMode()) {
            REPEAT_MODE_NONE -> setRepeatMode(REPEAT_MODE_ALL)
            REPEAT_MODE_ALL -> setRepeatMode(REPEAT_MODE_THIS)
            else -> setRepeatMode(REPEAT_MODE_NONE)
        }
    }

    fun toggleShuffle() {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            setShuffleMode(SHUFFLE_MODE_SHUFFLE)
        } else {
            setShuffleMode(SHUFFLE_MODE_NONE)
        }
    }

    private fun getShuffleMode(): Int {
        return shuffleMode
    }

    private fun setShuffleMode(shuffleMode: Int) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putInt(SAVED_SHUFFLE_MODE, shuffleMode)
            .apply()
        when (shuffleMode) {
            SHUFFLE_MODE_SHUFFLE -> {
                this.shuffleMode = shuffleMode
                ShuffleHelper.makeShuffleList(playingQueue, getPosition())
                position = 0
            }
            SHUFFLE_MODE_NONE -> {
                this.shuffleMode = shuffleMode
                val currentSongId = currentSong.id
                playingQueue = ArrayList(originalPlayingQueue)
                var newPosition = 0
                for (song in playingQueue) {
                    if (song.id == currentSongId) {
                        newPosition = playingQueue.indexOf(song)
                    }
                }
                position = newPosition
            }
        }
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED)
        notifyChange(QUEUE_CHANGED)
    }

    private fun notifyChange(what: String) {
        handleAndSendChangeInternal(what)
        sendPublicIntent(what)
    }

    private fun handleAndSendChangeInternal(what: String) {
        handleChangeInternal(what)
        sendChangeInternal(what)
    }

    // to let other apps know whats playing. i.E. last.fm (scrobbling) or musixmatch
    private fun sendPublicIntent(what: String) {
        val intent = Intent(what.replace(PHONOGRAPH_PACKAGE_NAME, MUSIC_PACKAGE_NAME))
        val song = currentSong
        intent.putExtra("id", song.id)
        intent.putExtra("artist", song.artistName)
        intent.putExtra("album", song.albumName)
        intent.putExtra("track", song.title)
        intent.putExtra("duration", song.duration)
        intent.putExtra("position", songProgressMillis.toLong())
        intent.putExtra("playing", isPlaying)
        intent.putExtra("scrobbling_source", PHONOGRAPH_PACKAGE_NAME)
        sendStickyBroadcast(intent)
    }

    fun sendChangeInternal(what: String?) {
        sendBroadcast(Intent(what))
        appWidgetBig.notifyChange(this, what)
        appWidgetClassic.notifyChange(this, what)
        appWidgetSmall.notifyChange(this, what)
        appWidgetCard.notifyChange(this, what)
    }

    private fun handleChangeInternal(what: String) {
        when (what) {
            PLAY_STATE_CHANGED -> {
                updateNotification()
                updateMediaSessionPlaybackState()
                val isPlaying = isPlaying
                if (!isPlaying && songProgressMillis > 0) {
                    savePositionInTrack()
                }
                if (countDownTimerPausable != null) {
                    if (isPlaying) {
                        countDownTimerPausable!!.start()
                    } else {
                        countDownTimerPausable!!.pause()
                    }
                }
                songPlayCountHelper.notifyPlayStateChanged(isPlaying)
            }
            META_CHANGED -> {
                updateNotification()
                updateMediaSessionMetaData()
                savePosition()
                savePositionInTrack()
                val currentSong = currentSong
                val r = Runnable {
                    HistoryStore.getInstance(this).addSongId(currentSong.id)
                    if (songPlayCountHelper.shouldBumpPlayCount()) {
                        SongPlayCountStore.getInstance(this)
                            .bumpPlayCount(songPlayCountHelper.song.id)
                    }
                    songPlayCountHelper.notifySongChanged(currentSong)
                    sendChangeInternal(META_CHANGED)
                }
                if (countDownTimerPausable != null) countDownTimerPausable!!.cancel()
                countDownTimerPausable = object : CountDownTimerPausable(
                    currentSong.duration / 8, 1000
                ) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        r.run()
                    }
                }
                countDownTimerPausable?.start()
            }
            QUEUE_CHANGED -> {
                updateMediaSessionMetaData() // because playing queue size might have changed
                saveState()
                if (playingQueue.size > 0) {
                    prepareNext()
                } else {
                    playingNotification!!.stop()
                }
            }
            MEDIA_STORE_CHANGED -> {
                playingQueue = getInstance(this).savedPlayingQueue.toMutableList()
                originalPlayingQueue = getInstance(this).savedOriginalPlayingQueue.toMutableList()
                updateNotification()
            }
        }
    }

    val audioSessionId: Int
        get() = playback.audioSessionId

    fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun acquireWakeLock(milli: Long) {
        wakeLock.acquire(milli)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceUtil.GAPLESS_PLAYBACK -> if (sharedPreferences.getBoolean(key, false)) {
                prepareNext()
            } else {
                playback.setNextDataSource(null)
            }
            PreferenceUtil.ALBUM_ART_ON_LOCKSCREEN -> updateMediaSessionMetaData()
            PreferenceUtil.COLORED_NOTIFICATION -> updateNotification()
            PreferenceUtil.CLASSIC_NOTIFICATION -> {
                initNotification()
                updateNotification()
            }
        }
    }

    override fun onTrackWentToNext() {
        playerHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT)
    }

    override fun onTrackEnded() {
        acquireWakeLock(30000)
        playerHandler.sendEmptyMessage(TRACK_ENDED)
    }

    private class PlaybackHandler(service: MusicService, looper: Looper) : Handler(looper) {
        private val mService: WeakReference<MusicService> = WeakReference(service)
        private var currentDuckVolume = 1.0f
        override fun handleMessage(msg: Message) {
            val service = mService.get() ?: return
            when (msg.what) {
                DUCK -> {
                    if (audioDucking()) {
                        currentDuckVolume -= .05f
                        if (currentDuckVolume > .2f) {
                            sendEmptyMessageDelayed(DUCK, 10)
                        } else {
                            currentDuckVolume = .2f
                        }
                    } else {
                        currentDuckVolume = 1f
                    }
                    service.playback.setVolume(currentDuckVolume)
                }
                UNDUCK -> {
                    if (audioDucking()) {
                        currentDuckVolume += .03f
                        if (currentDuckVolume < 1f) {
                            sendEmptyMessageDelayed(UNDUCK, 10)
                        } else {
                            currentDuckVolume = 1f
                        }
                    } else {
                        currentDuckVolume = 1f
                    }
                    service.playback.setVolume(currentDuckVolume)
                }
                TRACK_WENT_TO_NEXT -> if (service.pendingQuit || service.getRepeatMode() == REPEAT_MODE_NONE && service.isLastTrack) {
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
                    service.notifyChange(META_CHANGED)
                }
                TRACK_ENDED -> {
                    // if there is a timer finished, don't continue
                    if (service.pendingQuit ||
                        service.getRepeatMode() == REPEAT_MODE_NONE && service.isLastTrack
                    ) {
                        service.notifyChange(PLAY_STATE_CHANGED)
                        service.seek(0)
                        if (service.pendingQuit) {
                            service.pendingQuit = false
                            service.quit()
                            return
                        }
                    } else {
                        service.playNextSong(false)
                    }
                    sendEmptyMessage(RELEASE_WAKELOCK)
                }
                RELEASE_WAKELOCK -> service.releaseWakeLock()
                PLAY_SONG -> service.playSongAtImpl(msg.arg1)
                SET_POSITION -> {
                    val playing = service.isPlaying
                    if (service.openTrackAndPrepareNextAt(msg.arg1) && playing) {
                        service.play()
                    }
                }
                PREPARE_NEXT -> service.prepareNextImpl()
                RESTORE_QUEUES -> service.restoreQueuesAndPositionIfNecessary()
                FOCUS_CHANGE -> when (msg.arg1) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        if (!service.isPlaying && service.pausedByTransientLossOfFocus) {
                            service.play()
                            service.pausedByTransientLossOfFocus = false
                        }
                        removeMessages(DUCK)
                        sendEmptyMessage(UNDUCK)
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
                        removeMessages(UNDUCK)
                        sendEmptyMessage(DUCK)
                    }
                }
            }
        }

    }

    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    private val widgetIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val command = intent.getStringExtra(EXTRA_APP_WIDGET_NAME)
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            when (command) {
                AppWidgetClassic.NAME -> {
                    appWidgetClassic.performUpdate(this@MusicService, ids)
                }
                AppWidgetSmall.NAME -> {
                    appWidgetSmall.performUpdate(this@MusicService, ids)
                }
                AppWidgetBig.NAME -> {
                    appWidgetBig.performUpdate(this@MusicService, ids)
                }
                AppWidgetCard.NAME -> {
                    appWidgetCard.performUpdate(this@MusicService, ids)
                }
            }
        }
    }

    private inner class MediaStoreObserver(private val mHandler: Handler) : ContentObserver(
        mHandler
    ), Runnable {

        // milliseconds to delay before calling refresh to aggregate events
        private val REFRESH_DELAY: Long = 500

        override fun onChange(selfChange: Boolean) {
            // if a change is detected, remove any scheduled callback
            // then post a new one. This is intended to prevent closely
            // spaced events from generating multiple refresh calls
            mHandler.removeCallbacks(this)
            mHandler.postDelayed(this, REFRESH_DELAY)
        }

        override fun run() {
            // actually call refresh when the delayed callback fires
            // do not send a sticky broadcast here
            handleAndSendChangeInternal(MEDIA_STORE_CHANGED)
        }
    }

    private inner class ThrottledSeekHandler(private val mHandler: Handler) : Runnable {

        // milliseconds to throttle before calling run() to aggregate events
        private val THROTTLE: Long = 500

        fun notifySeek() {
            updateMediaSessionPlaybackState()
            mHandler.removeCallbacks(this)
            mHandler.postDelayed(this, THROTTLE)
        }

        override fun run() {
            savePositionInTrack()
            sendPublicIntent(PLAY_STATE_CHANGED) // for musixmatch synced lyrics
        }
    }

    private class SongPlayCountHelper {
        private val stopWatch = StopWatch()
        var song = emptySong
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

        companion object {
            val TAG = SongPlayCountHelper::class.java.simpleName
        }
    }

    companion object {
        const val PHONOGRAPH_PACKAGE_NAME = "com.o4x.musical"
        const val MUSIC_PACKAGE_NAME = "com.android.music"
        const val ACTION_TOGGLE_PAUSE = PHONOGRAPH_PACKAGE_NAME + ".togglepause"
        const val ACTION_PLAY = PHONOGRAPH_PACKAGE_NAME + ".play"
        const val ACTION_PLAY_PLAYLIST = PHONOGRAPH_PACKAGE_NAME + ".play.playlist"
        const val ACTION_PAUSE = PHONOGRAPH_PACKAGE_NAME + ".pause"
        const val ACTION_STOP = PHONOGRAPH_PACKAGE_NAME + ".stop"
        const val ACTION_SKIP = PHONOGRAPH_PACKAGE_NAME + ".skip"
        const val ACTION_REWIND = PHONOGRAPH_PACKAGE_NAME + ".rewind"
        const val ACTION_QUIT = PHONOGRAPH_PACKAGE_NAME + ".quitservice"
        const val ACTION_PENDING_QUIT = PHONOGRAPH_PACKAGE_NAME + ".pendingquitservice"
        const val INTENT_EXTRA_PLAYLIST = PHONOGRAPH_PACKAGE_NAME + "intentextra.playlist"
        const val INTENT_EXTRA_SHUFFLE_MODE = PHONOGRAPH_PACKAGE_NAME + ".intentextra.shufflemode"
        const val APP_WIDGET_UPDATE = PHONOGRAPH_PACKAGE_NAME + ".appwidgetupdate"
        const val EXTRA_APP_WIDGET_NAME = PHONOGRAPH_PACKAGE_NAME + "app_widget_name"

        // do not change these three strings as it will break support with other apps (e.g. last.fm scrobbling)
        const val META_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".metachanged"
        const val QUEUE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".queuechanged"
        const val PLAY_STATE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".playstatechanged"
        const val REPEAT_MODE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".repeatmodechanged"
        const val SHUFFLE_MODE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".shufflemodechanged"
        const val MEDIA_STORE_CHANGED = PHONOGRAPH_PACKAGE_NAME + ".mediastorechanged"
        const val SAVED_POSITION = "POSITION"
        const val SAVED_POSITION_IN_TRACK = "POSITION_IN_TRACK"
        const val SAVED_SHUFFLE_MODE = "SHUFFLE_MODE"
        const val SAVED_REPEAT_MODE = "REPEAT_MODE"
        const val RELEASE_WAKELOCK = 0
        const val TRACK_ENDED = 1
        const val TRACK_WENT_TO_NEXT = 2
        const val PLAY_SONG = 3
        const val PREPARE_NEXT = 4
        const val SET_POSITION = 5
        private const val FOCUS_CHANGE = 6
        private const val DUCK = 7
        private const val UNDUCK = 8
        const val RESTORE_QUEUES = 9
        const val SHUFFLE_MODE_NONE = 0
        const val SHUFFLE_MODE_SHUFFLE = 1
        const val REPEAT_MODE_NONE = 0
        const val REPEAT_MODE_ALL = 1
        const val REPEAT_MODE_THIS = 2
        const val SAVE_QUEUES = 0
        private fun getTrackUri(song: Song): String {
            return MusicUtil.getSongFileUri(song.id).toString()
        }

        private fun copy(bitmap: Bitmap): Bitmap? {
            var config = bitmap.config
            if (config == null) {
                config = Bitmap.Config.RGB_565
            }
            return try {
                bitmap.copy(config, false)
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                null
            }
        }

        private const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SEEK_TO)
    }
}