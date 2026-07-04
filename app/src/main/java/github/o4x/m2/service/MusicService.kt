package github.o4x.m2.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.media.audiofx.AudioEffect
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.preference.PreferenceManager
import github.o4x.m2.R
import github.o4x.m2.helper.CountDownTimerPausable
import github.o4x.m2.helper.ShuffleHelper
import github.o4x.m2.model.Playlist
import github.o4x.m2.model.Song
import github.o4x.m2.prefs.PreferenceUtil
import github.o4x.m2.prefs.PreferenceUtil.registerOnSharedPreferenceChangedListener
import github.o4x.m2.prefs.PreferenceUtil.unregisterOnSharedPreferenceChangedListener
import github.o4x.m2.repository.RoomRepository
import github.o4x.m2.repository.SongRepository
import github.o4x.m2.service.misc.MediaStoreObserver
import github.o4x.m2.service.misc.ThrottledSeekHandler
import github.o4x.m2.service.notification.PlayingNotification
import github.o4x.m2.service.notification.PlayingNotificationImpl
import github.o4x.m2.service.playback.Playback
import github.o4x.m2.service.player.Media3Playback
import github.o4x.m2.ui.activities.MainActivity
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.random.Random

/**
 * @author Karim Abou Zeid (kabouzeid), Andrew Neal, improved by AI
 */
class MusicService : Service(), SharedPreferences.OnSharedPreferenceChangeListener, Playback.PlaybackCallbacks {

    companion object {
        const val MUSICAL_PACKAGE_NAME = "github.o4x.m2"
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

        const val SHUFFLE_MODE_NONE = 0
        const val SHUFFLE_MODE_SHUFFLE = 1

        const val REPEAT_MODE_NONE = 0
        const val REPEAT_MODE_ALL = 1
        const val REPEAT_MODE_THIS = 2
    }

    private val roomRepository by inject<RoomRepository>()
    private val songRepository by inject<SongRepository>()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val musicBind: IBinder = MusicBinder()
    inner class MusicBinder : Binder() {
        val service: MusicService get() = this@MusicService
    }

    var pendingQuit = false

    private var playingNotification: PlayingNotification? = null
    lateinit var mediaSession: MediaSession

    private var countDownTimerPausable: CountDownTimerPausable? = null

    private val uiThreadHandler by lazy { Handler(Looper.getMainLooper()) }
    private val mediaStoreObserver by lazy { MediaStoreObserver(this, uiThreadHandler) }

    override fun onCreate() {
        super.onCreate()
        playback.setCallbacks(this)
        setupMediaSession()

        initNotification()

        mediaStoreObserver.start()
        registerOnSharedPreferenceChangedListener(this)
        restoreState()
        sendBroadcast(Intent("github.o4x.m2.MUSICAL_MUSIC_SERVICE_CREATED"))
    }

    private fun setupMediaSession() {
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        mediaSession = MediaSession.Builder(this, QueueNavigationPlayer())
            .setSessionActivity(sessionActivity)
            .build()
    }

    /**
     * The player handed to the media session. The underlying ExoPlayer only
     * holds the current (and next) track, so queue navigation from external
     * controllers (notification, headset, lockscreen) must be routed through
     * the service's queue logic instead of the player's playlist.
     */
    private inner class QueueNavigationPlayer : ForwardingPlayer(playback.player) {

        override fun play() = this@MusicService.play()

        override fun pause() = this@MusicService.pause()

        override fun setPlayWhenReady(playWhenReady: Boolean) {
            if (playWhenReady) this@MusicService.play() else this@MusicService.pause()
        }

        override fun stop() = quit()

        override fun seekToNext() = nextSong(true)

        override fun seekToNextMediaItem() = nextSong(true)

        override fun seekToPrevious() = back(true)

        override fun seekToPreviousMediaItem() = back(true)

        override fun seekTo(positionMs: Long) {
            seek(positionMs.toInt())
        }

        override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
            seek(positionMs.toInt())
        }

        override fun getAvailableCommands(): Player.Commands =
            super.getAvailableCommands().buildUpon()
                .addAll(
                    Player.COMMAND_PLAY_PAUSE,
                    Player.COMMAND_SEEK_TO_NEXT,
                    Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                    Player.COMMAND_SEEK_TO_PREVIOUS,
                    Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                    Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM
                )
                .build()

        override fun isCommandAvailable(command: Int): Boolean =
            availableCommands.contains(command)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        // Wait for the queue restore so actions always operate on the restored state.
        serviceScope.launch {
            restoreJob.join()
            when (action) {
                ACTION_TOGGLE_PAUSE -> if (isPlaying) pause() else play()
                ACTION_PAUSE -> pause()
                ACTION_PLAY -> play()
                ACTION_PLAY_PLAYLIST -> {
                    val playlist: Playlist? = intent.getParcelableExtra(INTENT_EXTRA_PLAYLIST)
                    val shuffle = intent.getIntExtra(INTENT_EXTRA_SHUFFLE_MODE, shuffleMode)
                    val songs = playlist?.let { withContext(Dispatchers.IO) { it.songs() } }
                    if (!songs.isNullOrEmpty()) {
                        playSongs(songs, shuffle)
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
        countDownTimerPausable?.cancel()
        countDownTimerPausable = null
        quit()
        releaseResources()
        mediaStoreObserver.cancel()
        unregisterOnSharedPreferenceChangedListener(this)
        serviceJob.cancel()
        sendBroadcast(Intent("github.o4x.m2.MUSICAL_MUSIC_SERVICE_DESTROYED"))
    }

    override fun onBind(intent: Intent): IBinder = musicBind

    private fun restoreState() {
        restorePlayerState()
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED)
        handleAndSendChangeInternal(REPEAT_MODE_CHANGED)
        restoreJob = serviceScope.launch { restoreQueuesAndPositionIfNecessary() }
    }

    fun quit() {
        pause()
        playingNotification?.stop()
        closeAudioEffectSession()
        stopSelf()
    }

    private fun releaseResources() {
        mediaSession.release()
        playback.release()
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
        sendBroadcast(intent)
    }

    fun sendChangeInternal(what: String?) {
        sendBroadcast(Intent(what))
    }

    private fun handleChangeInternal(what: String) {
        when (what) {
            PLAY_STATE_CHANGED -> {
                updateNotification()
                if (!isPlaying && songProgressMillis > 0) savePositionInTrack()

                countDownTimerPausable?.let {
                    if (isPlaying) it.start() else it.pause()
                }
            }
            META_CHANGED -> {
                updateNotification()
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
                saveState()
                if (playingQueue.isNotEmpty()) prepareNext() else playingNotification?.stop()
            }
            MEDIA_STORE_CHANGED -> {
                serviceScope.launch(Dispatchers.IO) {
                    val queue = roomRepository.savedPlayingQueue().toMutableList()
                    val originalQueue = roomRepository.savedOriginalPlayingQueue().toMutableList()
                    withContext(Dispatchers.Main) {
                        playingQueue = queue
                        originalPlayingQueue = originalQueue
                        updateNotification()
                    }
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceUtil.GAPLESS_PLAYBACK -> {
                if (sharedPreferences?.getBoolean(key, false) == true) prepareNext()
                else playback.setNextDataSource(null)
            }
            // ALBUM_ART_ON_LOCKSCREEN takes effect when the next track's
            // MediaItem is built; rebuild the queued one right away.
            PreferenceUtil.ALBUM_ART_ON_LOCKSCREEN -> prepareNext()
        }
    }

    /////////////
    // <QUEUE> //
    /////////////

    @JvmField var position = -1
    private var nextPosition = -1

    var playingQueue = mutableListOf<Song>()
    private var originalPlayingQueue = mutableListOf<Song>()

    private var queuesRestored = false
    private var notHandledMetaChangedForCurrentTrack = false

    private lateinit var restoreJob: Job
    private var queueSaveJob: Job? = null

    private suspend fun restoreQueuesAndPositionIfNecessary() {
        if (!queuesRestored && playingQueue.isEmpty()) {
            val restoredQueue = withContext(Dispatchers.IO) {
                roomRepository.savedPlayingQueue()
            }.toMutableList()
            val restoredOriginalQueue = withContext(Dispatchers.IO) {
                roomRepository.savedOriginalPlayingQueue()
            }.toMutableList()
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@MusicService)
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
            } else {
                fillEmptyQueueWithShuffledSongs()
            }
        }
        queuesRestored = true
    }

    /**
     * When there is nothing to restore (e.g. first launch), put all songs
     * shuffled into the queue instead of leaving it empty. Nothing starts
     * playing. On a fresh install the service may be created before the
     * media permission is granted; [fillEmptyQueueWithShuffledSongsAsync]
     * retries once the permission comes through.
     */
    private suspend fun fillEmptyQueueWithShuffledSongs() {
        val allSongs = withContext(Dispatchers.IO) { songRepository.songs() }
        if (allSongs.isEmpty() || playingQueue.isNotEmpty()) return

        val shuffled = allSongs.toMutableList()
        ShuffleHelper.makeShuffleList(shuffled, -1)
        originalPlayingQueue = shuffled.toMutableList()
        playingQueue = shuffled.toMutableList()
        position = 0

        openCurrent()
        prepareNext()
        notHandledMetaChangedForCurrentTrack = true
        saveState()
        sendChangeInternal(META_CHANGED)
        sendChangeInternal(QUEUE_CHANGED)
    }

    fun fillEmptyQueueWithShuffledSongsAsync() {
        serviceScope.launch {
            restoreJob.join()
            fillEmptyQueueWithShuffledSongs()
        }
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
        val queue = playingQueue.toList()
        val originalQueue = originalPlayingQueue.toList()
        queueSaveJob?.cancel()
        queueSaveJob = serviceScope.launch(Dispatchers.IO) {
            roomRepository.saveQueue(queue)
            roomRepository.saveOriginalQueue(originalQueue)
        }
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

        if (shuffleMode == SHUFFLE_MODE_NONE) {
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
        if (shuffleMode == SHUFFLE_MODE_NONE) {
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
        var duration = 0L
        for (i in position + 1 until playingQueue.size) {
            duration += playingQueue[i].duration
        }
        return duration
    }

    private fun getSongAt(position: Int): Song {
        return if (position in playingQueue.indices) playingQueue[position] else Song.emptySong
    }

    //////////////
    // <PLAYER> //
    //////////////

    val playback: Media3Playback by lazy { Media3Playback(this) }

    @JvmField var shuffleMode = 0
    @JvmField var repeatMode = 0

    val songProgressMillis: Int get() = playback.position()
    val songDurationMillis: Int get() = playback.duration()

    private val throttledSeekHandler: ThrottledSeekHandler by lazy { ThrottledSeekHandler(this, uiThreadHandler) }

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
        setShuffleMode(if (shuffleMode == SHUFFLE_MODE_NONE) SHUFFLE_MODE_SHUFFLE else SHUFFLE_MODE_NONE)
    }

    fun setShuffleMode(shuffleMode: Int) {
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
        if (playback.isPlaying) return
        if (!playback.isInitialized) {
            playSongAt(position)
        } else {
            playback.start()
            if (notHandledMetaChangedForCurrentTrack) {
                handleChangeInternal(META_CHANGED)
                notHandledMetaChangedForCurrentTrack = false
            }
        }
    }

    fun pause() {
        playback.pause()
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
        val wasPlaying = isPlaying
        if (openTrackAndPrepareNextAt(position) && wasPlaying) {
            play()
        }
    }

    fun playSongs(songs: List<Song>, shuffleMode: Int, startPlaying: Boolean = true) {
        if (songs.isNotEmpty()) {
            originalPlayingQueue = songs.toMutableList()
            playingQueue = songs.toMutableList()
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                ShuffleHelper.makeShuffleList(playingQueue, Random.nextInt(songs.size))
            }
            position = 0
            this.shuffleMode = shuffleMode
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(SAVED_SHUFFLE_MODE, shuffleMode).apply()
            notifyChange(QUEUE_CHANGED)
            handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED)
            if (startPlaying) playSongAt(0) else setPosition(0)
        } else {
            Toast.makeText(applicationContext, R.string.playlist_is_empty, Toast.LENGTH_LONG).show()
        }
    }

    fun nextSong(force: Boolean) = setPosition(getNextPosition(force))
    fun playNextSong(force: Boolean) = playSongAt(getNextPosition(force))

    private fun openCurrent(): Boolean {
        return try {
            playback.setDataSource(currentSong)
        } catch (e: Exception) {
            false
        }
    }

    fun playSongAt(position: Int) {
        if (openTrackAndPrepareNextAt(position)) {
            play()
        } else {
            Toast.makeText(this, resources.getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show()
        }
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
        this.position = position
        val prepared = openCurrent()
        if (prepared) prepareNext()
        notifyChange(META_CHANGED)
        notHandledMetaChangedForCurrentTrack = false
        return prepared
    }

    fun prepareNext(): Boolean {
        return try {
            val nextPos = getNextPosition(false)
            playback.setNextDataSource(getSongAt(nextPos))
            this.nextPosition = nextPos
            true
        } catch (e: Exception) {
            false
        }
    }

    fun previousSong(force: Boolean) = setPosition(getPreviousPosition(force))

    fun seek(millis: Int): Int {
        return try {
            val newPosition = playback.seek(millis)
            throttledSeekHandler.notifySeek()
            newPosition
        } catch (e: Exception) {
            -1
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
        if (pendingQuit || repeatMode == REPEAT_MODE_NONE && isLastTrack) {
            pause()
            seek(0)
            if (pendingQuit) {
                pendingQuit = false
                quit()
            }
        } else {
            position = nextPosition
            prepareNext()
            notifyChange(META_CHANGED)
        }
    }

    override fun onTrackEnded() {
        // if there is a timer finished, don't continue
        if (pendingQuit || repeatMode == REPEAT_MODE_NONE && isLastTrack) {
            // Make sure playWhenReady is off before rewinding, otherwise the
            // seek out of the ended state would restart playback.
            pause()
            seek(0)
            if (pendingQuit) {
                pendingQuit = false
                quit()
            }
        } else {
            playNextSong(false)
        }
    }

    override fun onPlayStateChanged() {
        notifyChange(PLAY_STATE_CHANGED)
    }
}
