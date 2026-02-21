package github.o4x.musical.service

import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import github.o4x.musical.App.Companion.getContext
import github.o4x.musical.R
import github.o4x.musical.helper.CountDownTimerPausable
import github.o4x.musical.helper.ShuffleHelper
import github.o4x.musical.imageloader.glide.loader.GlideLoader.Companion.with
import github.o4x.musical.imageloader.glide.targets.CustomBitmapTarget
import github.o4x.musical.model.Playlist
import github.o4x.musical.model.Song
import github.o4x.musical.prefs.PreferenceUtil
import github.o4x.musical.prefs.PreferenceUtil.albumArtOnLockscreen
import github.o4x.musical.prefs.PreferenceUtil.registerOnSharedPreferenceChangedListener
import github.o4x.musical.prefs.PreferenceUtil.unregisterOnSharedPreferenceChangedListener
import github.o4x.musical.repository.RoomRepository
import github.o4x.musical.service.misc.MediaStoreObserver
import github.o4x.musical.service.misc.QueueSaveHandler
import github.o4x.musical.service.misc.ThrottledSeekHandler
import github.o4x.musical.service.notification.PlayingNotification
import github.o4x.musical.service.notification.PlayingNotificationImpl
import github.o4x.musical.service.playback.Playback
import github.o4x.musical.service.playback.PlaybackHandler
import github.o4x.musical.service.player.MultiPlayer
import github.o4x.musical.util.MusicUtil
import github.o4x.musical.util.Util
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.random.Random

/**
 * @author Karim Abou Zeid (kabouzeid), Andrew Neal, improved by AI
 */
class MusicService : Service(), SharedPreferences.OnSharedPreferenceChangeListener, Playback.PlaybackCallbacks {

    companion object {
        const val MUSICAL_PACKAGE_NAME = "github.o4x.musical"
        const val MUSIC_PACKAGE_NAME = "com.android.music"

        const val ACTION_TOGGLE_PAUSE = "$MUSICAL_PACKAGE_NAME.togglepause"
        const val ACTION_PLAY = "$MUSICAL_PACKAGE_NAME.play"
        const val ACTION_PLAY_PLAYLIST = "$MUSICAL_PACKAGE_NAME.play.playlist"
        const val ACTION_PAUSE = "$MUSICAL_PACKAGE_NAME.pause"
        const val ACTION_STOP = "$MUSICAL_PACKAGE_NAME.stop"
        const val ACTION_SKIP = "$MUSICAL_PACKAGE_NAME.skip"
        const val ACTION_REWIND = "$MUSICAL_PACKAGE_NAME.rewind"
        const val ACTION_QUIT = "$MUSICAL_PACKAGE_NAME.quitservice"
        const val ACTION_PENDING_QUIT = "$MUSICAL_PACKAGE_NAME.pendingquitservice"

        const val INTENT_EXTRA_PLAYLIST = "$MUSICAL_PACKAGE_NAME.intentextra.playlist"
        const val INTENT_EXTRA_SHUFFLE_MODE = "$MUSICAL_PACKAGE_NAME.intentextra.shufflemode"

        const val META_CHANGED = "$MUSICAL_PACKAGE_NAME.metachanged"
        const val QUEUE_CHANGED = "$MUSICAL_PACKAGE_NAME.queuechanged"
        const val PLAY_STATE_CHANGED = "$MUSICAL_PACKAGE_NAME.playstatechanged"
        const val REPEAT_MODE_CHANGED = "$MUSICAL_PACKAGE_NAME.repeatmodechanged"
        const val SHUFFLE_MODE_CHANGED = "$MUSICAL_PACKAGE_NAME.shufflemodechanged"
        const val MEDIA_STORE_CHANGED = "$MUSICAL_PACKAGE_NAME.mediastorechanged"

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
        const val FOCUS_CHANGE = 6
        const val DUCK = 7
        const val UNDUCK = 8
        const val RESTORE_QUEUES = 9
        const val SAVE_QUEUES = 0

        const val SHUFFLE_MODE_NONE = 0
        const val SHUFFLE_MODE_SHUFFLE = 1

        const val REPEAT_MODE_NONE = 0
        const val REPEAT_MODE_ALL = 1
        const val REPEAT_MODE_THIS = 2

        private fun getTrackUri(song: Song): String {
            return MusicUtil.getFileUriFromSong(song.id).toString()
        }

        private const val MEDIA_SESSION_ACTIONS = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_SEEK_TO)
    }

    private val roomRepository by inject<RoomRepository>()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val musicBind: IBinder = MusicBinder()
    inner class MusicBinder : Binder() {
        val service: MusicService get() = this@MusicService
    }

    var pendingQuit = false

    private var playingNotification: PlayingNotification? = null
    lateinit var mediaSession: MediaSessionCompat

    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Musical::PlaybackWakeLock")
    }

    private var countDownTimerPausable: CountDownTimerPausable? = null
    private var becomingNoisyReceiverRegistered = false

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }

    private val mediaStoreObserver by lazy { MediaStoreObserver(this, playerHandler) }
    private val uiThreadHandler by lazy { Handler(Looper.getMainLooper()) }
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onCreate() {
        super.onCreate()
        wakeLock.setReferenceCounted(false)
        musicPlayerHandlerThread.start()
        playback.setCallbacks(this)
        setupMediaSession()

        queueSaveHandlerThread.start()
        initNotification()

        mediaStoreObserver.start()
        registerOnSharedPreferenceChangedListener(this)
        restoreState()
        mediaSession.isActive = true
        sendBroadcast(Intent("github.o4x.musical.MUSICAL_MUSIC_SERVICE_CREATED"))
    }

    private fun setupMediaSession() {
        val mediaButtonReceiverComponentName = ComponentName(applicationContext, MediaButtonIntentReceiver::class.java)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.component = mediaButtonReceiverComponentName
        val mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            mediaButtonIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        mediaSession = MediaSessionCompat(
            this,
            "Musical",
            mediaButtonReceiverComponentName,
            mediaButtonReceiverPendingIntent
        ).apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = play()
                override fun onPause() = pause()
                override fun onSkipToNext() = nextSong(true)
                override fun onSkipToPrevious() = back(true)
                override fun onStop() = quit()
                override fun onSeekTo(pos: Long) { seek(pos.toInt()) }
                override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                    return MediaButtonIntentReceiver.handleIntent(this@MusicService, mediaButtonEvent)
                }
            })
            setMediaButtonReceiver(mediaButtonReceiverPendingIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null) {
            restoreQueuesAndPositionIfNecessary()
            when (intent.action) {
                ACTION_TOGGLE_PAUSE -> if (isPlaying) pause() else play()
                ACTION_PAUSE -> pause()
                ACTION_PLAY -> play()
                ACTION_PLAY_PLAYLIST -> {
                    val playlist: Playlist? = intent.getParcelableExtra(INTENT_EXTRA_PLAYLIST)
                    val shuffleMode = intent.getIntExtra(INTENT_EXTRA_SHUFFLE_MODE, getShuffleMode())
                    if (playlist != null && playlist.songs().isNotEmpty()) {
                        val playlistSongs = playlist.songs()
                        val startPos = if (shuffleMode == SHUFFLE_MODE_SHUFFLE) Random.nextInt(playlistSongs.size) else 0
                        openQueue(playlistSongs, startPos, true)
                        if (shuffleMode == SHUFFLE_MODE_SHUFFLE) setShuffleMode(shuffleMode)
                    } else {
                        Toast.makeText(applicationContext, R.string.playlist_is_empty, Toast.LENGTH_LONG).show()
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
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (becomingNoisyReceiverRegistered) {
            unregisterReceiver(becomingNoisyReceiver)
            becomingNoisyReceiverRegistered = false
        }
        mediaSession.isActive = false
        quit()
        releaseResources()
        mediaStoreObserver.cancel()
        unregisterOnSharedPreferenceChangedListener(this)
        wakeLock.release()
        serviceJob.cancel()
        sendBroadcast(Intent("github.o4x.musical.MUSICAL_MUSIC_SERVICE_DESTROYED"))
    }

    override fun onBind(intent: Intent): IBinder = musicBind

    private fun restoreState() {
        restorePlayerState()
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED)
        handleAndSendChangeInternal(REPEAT_MODE_CHANGED)
        playerHandler.removeMessages(RESTORE_QUEUES)
        playerHandler.sendEmptyMessage(RESTORE_QUEUES)
    }

    fun quit() {
        pause()
        playingNotification?.stop()
        closeAudioEffectSession()
        abandonAudioFocus()
        stopSelf()
    }

    private fun releaseResources() {
        playerHandler.removeCallbacksAndMessages(null)
        musicPlayerHandlerThread.quitSafely()
        queueSaveHandler.removeCallbacksAndMessages(null)
        queueSaveHandlerThread.quitSafely()
        playback.release()
        mediaSession.release()
    }

    private fun closeAudioEffectSession() {
        val audioEffectsIntent = Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playback.audioSessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        }
        sendBroadcast(audioEffectsIntent)
    }

    private fun initNotification() {
        playingNotification = PlayingNotificationImpl().apply { init(this@MusicService) }
    }

    private fun updateNotification() {
        if (playingNotification != null && currentSong.id != -1L) {
            playingNotification?.start()
            playingNotification?.update()
        }
    }

    fun updateMediaSessionPlaybackState() {
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
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, position + 1L)
            .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.year.toLong())
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, playingQueue.size.toLong())
        }

        if (albumArtOnLockscreen()) {
            serviceScope.launch(Dispatchers.IO) {
                with(getContext()).load(song).into(object : CustomBitmapTarget(Util.getScreenWidth(), Util.getScreenHeight()) {
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

    // Modernized to use Coroutines instead of hard Thread spawning
    fun runOnNewThread(runnable: Runnable) {
        serviceScope.launch(Dispatchers.IO) { runnable.run() }
    }

    fun notifyChange(what: String) {
        handleAndSendChangeInternal(what)
        sendPublicIntent(what)
    }

    fun handleAndSendChangeInternal(what: String) {
        handleChangeInternal(what)
        sendChangeInternal(what)
    }

    fun sendPublicIntent(what: String) {
        val intent = Intent(what.replace(MUSICAL_PACKAGE_NAME, MUSIC_PACKAGE_NAME)).apply {
            val song = currentSong
            putExtra("id", song.id)
            putExtra("artist", song.artistName)
            putExtra("album", song.albumName)
            putExtra("track", song.title)
            putExtra("duration", song.duration)
            putExtra("position", songProgressMillis.toLong())
            putExtra("playing", isPlaying)
            putExtra("scrobbling_source", MUSICAL_PACKAGE_NAME)
        }
        // Replaced deprecated sendStickyBroadcast with standard broadcast
        sendBroadcast(intent)
    }

    fun sendChangeInternal(what: String?) {
        sendBroadcast(Intent(what))
    }

    private fun handleChangeInternal(what: String) {
        when (what) {
            PLAY_STATE_CHANGED -> {
                updateNotification()
                updateMediaSessionPlaybackState()
                if (!isPlaying && songProgressMillis > 0) savePositionInTrack()

                countDownTimerPausable?.let {
                    if (isPlaying) it.start() else it.pause()
                }
            }
            META_CHANGED -> {
                updateNotification()
                updateMediaSessionMetaData()
                savePosition()
                savePositionInTrack()
                val currentSongObj = currentSong

                countDownTimerPausable?.cancel()
                countDownTimerPausable = object : CountDownTimerPausable(currentSongObj.duration / 8, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        serviceScope.launch(Dispatchers.IO) {
                            roomRepository.addPlayCount(currentSongObj)
                            sendChangeInternal(META_CHANGED)
                        }
                    }
                }
                countDownTimerPausable?.start()
            }
            QUEUE_CHANGED -> {
                updateMediaSessionMetaData()
                saveState()
                if (playingQueue.isNotEmpty()) prepareNext() else playingNotification?.stop()
            }
            MEDIA_STORE_CHANGED -> {
                playingQueue = roomRepository.savedPlayingQueue().toMutableList()
                originalPlayingQueue = roomRepository.savedOriginalPlayingQueue().toMutableList()
                updateNotification()
            }
        }
    }

    fun releaseWakeLock() {
        if (wakeLock.isHeld) wakeLock.release()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceUtil.GAPLESS_PLAYBACK -> {
                if (sharedPreferences?.getBoolean(key, false) == true) prepareNext()
                else playback.setNextDataSource(null)
            }
            PreferenceUtil.ALBUM_ART_ON_LOCKSCREEN -> updateMediaSessionMetaData()
            PreferenceUtil.COLORED_NOTIFICATION -> updateNotification()
        }
    }

    /////////////
    // <QUEUE> //
    /////////////

    @JvmField var position = -1
    var nextPosition = -1

    var playingQueue = mutableListOf<Song>()
    private var originalPlayingQueue = mutableListOf<Song>()

    private var queuesRestored = false
    private var notHandledMetaChangedForCurrentTrack = false

    private val queueSaveHandler: QueueSaveHandler by lazy { QueueSaveHandler(this, queueSaveHandlerThread.looper) }
    private val queueSaveHandlerThread: HandlerThread by lazy { HandlerThread("QueueSaveHandler", Process.THREAD_PRIORITY_BACKGROUND) }

    @Synchronized
    fun restoreQueuesAndPositionIfNecessary() {
        if (!queuesRestored && playingQueue.isEmpty()) {
            val restoredQueue = roomRepository.savedPlayingQueue().toMutableList()
            val restoredOriginalQueue = roomRepository.savedOriginalPlayingQueue().toMutableList()
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val restoredPosition = prefs.getInt(SAVED_POSITION, -1)
            val restoredPositionInTrack = prefs.getInt(SAVED_POSITION_IN_TRACK, -1)

            if (restoredQueue.isNotEmpty() && restoredQueue.size == restoredOriginalQueue.size && restoredPosition != -1) {
                originalPlayingQueue = restoredOriginalQueue
                playingQueue = restoredQueue
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

    fun openQueue(newPlayingQueue: List<Song>?, startPosition: Int, startPlaying: Boolean) {
        if (!newPlayingQueue.isNullOrEmpty() && startPosition in newPlayingQueue.indices) {
            originalPlayingQueue = newPlayingQueue.toMutableList()
            playingQueue = originalPlayingQueue.toMutableList()
            var currentPos = startPosition

            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                ShuffleHelper.makeShuffleList(playingQueue, startPosition)
                currentPos = 0
            }
            if (startPlaying) playSongAt(currentPos) else setPosition(currentPos)
            notifyChange(QUEUE_CHANGED)
        }
    }

    fun saveQueuesImpl() {
        roomRepository.saveQueue(playingQueue)
        roomRepository.saveOriginalQueue(originalPlayingQueue)
    }

    private fun saveState() {
        saveQueues()
        savePosition()
        savePositionInTrack()
    }

    private fun savePosition() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION, position).apply()
    }

    fun savePositionInTrack() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION_IN_TRACK, songProgressMillis).apply()
    }

    private fun saveQueues() {
        queueSaveHandler.removeMessages(SAVE_QUEUES)
        queueSaveHandler.sendEmptyMessage(SAVE_QUEUES)
    }

    private fun rePosition(deletedPosition: Int) {
        if (deletedPosition < position) position--
        else if (deletedPosition == position) setPosition(if (playingQueue.size > deletedPosition) position else position - 1)
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

    fun moveSong(from: Int, to: Int) {
        if (from == to) return
        val currentPosition = position
        playingQueue.add(to, playingQueue.removeAt(from))

        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            originalPlayingQueue.add(to, originalPlayingQueue.removeAt(from))
        }

        when {
            currentPosition in to until from -> position++
            currentPosition in (from + 1)..to -> position--
            from == currentPosition -> position = to
        }
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
        val playIndex = playingQueue.indexOfFirst { it.id == song.id }
        if (playIndex != -1) {
            playingQueue.removeAt(playIndex)
            rePosition(playIndex)
        }
        originalPlayingQueue.removeAll { it.id == song.id }
        notifyChange(QUEUE_CHANGED)
    }

    fun clearQueue() {
        playingQueue.clear()
        originalPlayingQueue.clear()
        setPosition(-1)
        notifyChange(QUEUE_CHANGED)
    }

    fun getQueueDurationMillis(position: Int): Long {
        return playingQueue.drop(position + 1).sumOf { it.duration }
    }

    private fun getSongAt(position: Int): Song {
        return if (position in playingQueue.indices) playingQueue[position] else Song.emptySong
    }

    //////////////
    // <PLAYER> //
    //////////////

    val playback: Playback by lazy { MultiPlayer(this) }

    @JvmField var shuffleMode = 0
    @JvmField var repeatMode = 0
    var pausedByTransientLossOfFocus = false

    val songProgressMillis: Int get() = playback.position()
    val songDurationMillis: Int get() = playback.duration()

    private val becomingNoisyReceiverIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private val musicPlayerHandlerThread: HandlerThread by lazy { HandlerThread("PlaybackHandler") }
    private val playerHandler: PlaybackHandler by lazy { PlaybackHandler(this, musicPlayerHandlerThread.looper) }
    private val throttledSeekHandler: ThrottledSeekHandler by lazy { ThrottledSeekHandler(this, playerHandler) }

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        playerHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget()
    }

    private val audioManager: AudioManager by lazy { getSystemService(Service.AUDIO_SERVICE) as AudioManager }

    // Modernized Audio Focus Request
    fun requestFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusListener)
        }
    }

    fun restorePlayerState() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        shuffleMode = prefs.getInt(SAVED_SHUFFLE_MODE, 0)
        repeatMode = prefs.getInt(SAVED_REPEAT_MODE, 0)
    }

    fun setRepeatMode(repeatMode: Int) {
        if (repeatMode in REPEAT_MODE_NONE..REPEAT_MODE_THIS) {
            this.repeatMode = repeatMode
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_REPEAT_MODE, repeatMode).apply()
            prepareNext()
            handleAndSendChangeInternal(REPEAT_MODE_CHANGED)
        }
    }

    fun toggleShuffle() {
        setShuffleMode(if (getShuffleMode() == SHUFFLE_MODE_NONE) SHUFFLE_MODE_SHUFFLE else SHUFFLE_MODE_NONE)
    }

    private fun getShuffleMode(): Int = shuffleMode

    private fun setShuffleMode(shuffleMode: Int) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_SHUFFLE_MODE, shuffleMode).apply()
        this.shuffleMode = shuffleMode

        when (shuffleMode) {
            SHUFFLE_MODE_SHUFFLE -> {
                ShuffleHelper.makeShuffleList(playingQueue, position)
                position = 0
            }
            SHUFFLE_MODE_NONE -> {
                val currentSongId = currentSong.id
                playingQueue = originalPlayingQueue.toMutableList()
                position = playingQueue.indexOfFirst { it.id == currentSongId }.coerceAtLeast(0)
            }
        }
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED)
        notifyChange(QUEUE_CHANGED)
    }

    fun play() {
        synchronized(this) {
            if (requestFocus()) {
                if (!playback.isPlaying) {
                    if (!playback.isInitialized) {
                        playSongAt(position)
                    } else {
                        playback.start()
                        if (!becomingNoisyReceiverRegistered) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Important: ACTION_AUDIO_BECOMING_NOISY is a system broadcast, so we use RECEIVER_NOT_EXPORTED for security on modern Android.
                                ContextCompat.registerReceiver(this, becomingNoisyReceiver, becomingNoisyReceiverIntentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
                            } else {
                                registerReceiver(becomingNoisyReceiver, becomingNoisyReceiverIntentFilter)
                            }
                            becomingNoisyReceiverRegistered = true
                        }
                        if (notHandledMetaChangedForCurrentTrack) {
                            handleChangeInternal(META_CHANGED)
                            notHandledMetaChangedForCurrentTrack = false
                        }
                        notifyChange(PLAY_STATE_CHANGED)

                        playerHandler.removeMessages(DUCK)
                        playerHandler.sendEmptyMessage(UNDUCK)
                    }
                }
            } else {
                Toast.makeText(this, resources.getString(R.string.audio_focus_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun pause() {
        pausedByTransientLossOfFocus = false
        if (playback.isPlaying) {
            playback.pause()
            notifyChange(PLAY_STATE_CHANGED)
        }
    }

    private fun getPreviousPosition(force: Boolean): Int {
        var newPosition = position - 1
        when (repeatMode) {
            REPEAT_MODE_ALL -> if (newPosition < 0) newPosition = playingQueue.size - 1
            REPEAT_MODE_THIS -> if (force && newPosition < 0) newPosition = playingQueue.size - 1 else if (!force) newPosition = position
            else -> if (newPosition < 0) newPosition = 0
        }
        return newPosition
    }

    fun setPosition(position: Int) {
        playerHandler.removeMessages(SET_POSITION)
        playerHandler.obtainMessage(SET_POSITION, position, 0).sendToTarget()
    }

    fun playSongs(songs: List<Song>, shuffleMode: Int) {
        if (songs.isNotEmpty()) {
            val startPos = if (shuffleMode == SHUFFLE_MODE_SHUFFLE) Random.nextInt(songs.size) else 0
            openQueue(songs, startPos, false)
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) setShuffleMode(shuffleMode)
            play()
        } else {
            Toast.makeText(applicationContext, R.string.playlist_is_empty, Toast.LENGTH_LONG).show()
        }
    }

    fun nextSong(force: Boolean) = setPosition(getNextPosition(force))
    fun playNextSong(force: Boolean) = playSongAt(getNextPosition(force))

    private fun openCurrent(): Boolean {
        synchronized(this) {
            return try {
                playback.setDataSource(getTrackUri(currentSong))
            } catch (e: Exception) {
                false
            }
        }
    }

    fun playSongAt(position: Int) {
        playerHandler.removeMessages(PLAY_SONG)
        playerHandler.obtainMessage(PLAY_SONG, position, 0).sendToTarget()
    }

    fun playSongAtImpl(position: Int) {
        if (openTrackAndPrepareNextAt(position)) play()
        else Toast.makeText(this, resources.getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show()
    }

    private fun getNextPosition(force: Boolean): Int {
        var pos = position + 1
        when (repeatMode) {
            REPEAT_MODE_ALL -> if (isLastTrack) pos = 0
            REPEAT_MODE_THIS -> if (force && isLastTrack) pos = 0 else if (!force) pos -= 1
            else -> if (isLastTrack) pos -= 1
        }
        return pos
    }

    fun openTrackAndPrepareNextAt(position: Int): Boolean {
        synchronized(this) {
            this.position = position
            val prepared = openCurrent()
            if (prepared) prepareNextImpl()
            notifyChange(META_CHANGED)
            notHandledMetaChangedForCurrentTrack = false
            return prepared
        }
    }

    fun prepareNextImpl(): Boolean {
        synchronized(this) {
            return try {
                val nextPos = getNextPosition(false)
                playback.setNextDataSource(getTrackUri(getSongAt(nextPos)))
                this.nextPosition = nextPos
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun prepareNext() {
        playerHandler.removeMessages(PREPARE_NEXT)
        playerHandler.obtainMessage(PREPARE_NEXT).sendToTarget()
    }

    fun previousSong(force: Boolean) = setPosition(getPreviousPosition(force))

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

    fun back(force: Boolean) {
        if (songProgressMillis > 5000) seek(0) else previousSong(force)
    }

    fun cycleRepeatMode() {
        setRepeatMode(when (repeatMode) {
            REPEAT_MODE_NONE -> REPEAT_MODE_ALL
            REPEAT_MODE_ALL -> REPEAT_MODE_THIS
            else -> REPEAT_MODE_NONE
        })
    }

    val isPlaying: Boolean get() = playback.isPlaying
    val isLastTrack: Boolean get() = position == playingQueue.size - 1
    val currentSong: Song get() = getSongAt(position)
    val audioSessionId: Int get() = playback.audioSessionId

    override fun onTrackWentToNext() {
        playerHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT)
    }

    override fun onTrackEnded() {
        acquireWakeLock(30000)
        playerHandler.sendEmptyMessage(TRACK_ENDED)
    }

    private fun acquireWakeLock(milli: Long) {
        wakeLock.acquire(milli)
    }
}
