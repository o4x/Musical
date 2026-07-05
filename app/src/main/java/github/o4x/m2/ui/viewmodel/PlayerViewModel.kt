package github.o4x.m2.ui.viewmodel

import android.os.Looper
import android.widget.SeekBar
import androidx.lifecycle.*
import github.o4x.m2.App
import github.o4x.m2.helper.MusicPlayerRemote
import github.o4x.m2.helper.MusicProgressViewUpdateHelper
import github.o4x.m2.imageloader.glide.loader.GlideLoader
import github.o4x.m2.imageloader.glide.targets.CustomBitmapTarget
import github.o4x.m2.imageloader.glide.targets.palette.NotificationPaletteTargetListener
import github.o4x.m2.interfaces.MusicServiceEventListener
import github.o4x.m2.model.Song
import github.o4x.m2.util.MusicUtil
import github.o4x.m2.util.color.MediaNotificationProcessor

class PlayerViewModel : ViewModel(),
    MusicProgressViewUpdateHelper.Callback,
    SeekBar.OnSeekBarChangeListener, MusicServiceEventListener {

    // Deliver immediately when already on the main thread so a freshly registered
    // observer receives the value synchronously (no empty-first-frame flash);
    // fall back to postValue when called off the main thread.
    private fun <T> MutableLiveData<T>.publish(value: T) {
        if (Looper.myLooper() == Looper.getMainLooper()) this.value = value
        else this.postValue(value)
    }

      ///////////////////
     // PROGRESS PART //
    ///////////////////

    private val _progress = MutableLiveData<Int>()
    private val _total = MutableLiveData<Int>()

    private fun updateTimes() {
        _progress.publish(MusicPlayerRemote.position)
        _total.publish(MusicPlayerRemote.songDurationMillis)
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        _progress.publish(progress)
        _total.publish(total)
    }

    fun pause() {
        progressViewUpdateHelper.stop()
    }
    fun resume() {
        progressViewUpdateHelper.start()
    }

    val progress: LiveData<Int> = _progress
    val total: LiveData<Int> = _total

    val progressText: LiveData<String> =
        _progress.map { MusicUtil.getReadableDurationString(it.toLong()) }
    val totalText: LiveData<String> =
        _total.map { MusicUtil.getReadableDurationString(it.toLong()) }


    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        _progress.value = progress
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        pause()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        MusicPlayerRemote.seekTo(seekBar.progress)
        resume()
    }

      ////////////////
     // STATE PART //
    ////////////////

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying
    private fun updateIsPlaying() {
        _isPlaying.publish(MusicPlayerRemote.isPlaying)
    }

    private val _repeatMode = MutableLiveData<Int>()
    val repeatMode: LiveData<Int> = _repeatMode
    private fun updateRepeatMode() {
        _repeatMode.publish(MusicPlayerRemote.repeatMode)
    }

    private val _shuffleMode = MutableLiveData<Int>()
    val shuffleMode: LiveData<Int> = _shuffleMode
    private fun updateShuffleMode() {
        _shuffleMode.publish(MusicPlayerRemote.shuffleMode)
    }

      ///////////
     // QUEUE //
    ///////////

    private val _queue = MutableLiveData<List<Song>>()
    val queue: LiveData<List<Song>> = _queue
    private fun updateQueue() {
        _queue.publish(MusicPlayerRemote.playingQueue)
    }

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int> = _position
    private fun updatePosition() {
        MusicPlayerRemote.position.let {
            if (it >= 0) {
                _position.publish(it)
            }
        }
    }

      //////////////////
     // CURRENT SONG //
    //////////////////

    private val _currentSong = MutableLiveData<Song>().also {
        it.value = MusicPlayerRemote.currentSong
    }
    val currentSong: LiveData<Song> = _currentSong
    private fun updateCurrentSong() {
        _currentSong.publish(MusicPlayerRemote.currentSong)
    }

      //////////
     // INIT //
    //////////

    private val progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    private var isConnected = false

    init {
        isConnected = MusicPlayerRemote.musicService != null
        if (isConnected) {
            updateAll()
        }
        progressViewUpdateHelper.start()
    }

    private fun updateAll() {
        updateTimes()
        updateIsPlaying()
        updateRepeatMode()
        updateShuffleMode()
        updateQueue()
        updatePosition()
        updateCurrentSong()
    }

    override fun onCleared() {
        progressViewUpdateHelper.stop()
        super.onCleared()
    }

    ///////////////////////
    // SERVICE CALLBACKS //
    ///////////////////////

    override fun onServiceConnected() {
        if (!isConnected) {
            updateAll()
        }
    }

    override fun onServiceDisconnected() {}

    override fun onQueueChanged() {
        updateQueue()
    }

    override fun onPlayingMetaChanged() {
        updatePosition()
        updateCurrentSong()
    }

    override fun onPlayStateChanged() {
        updateIsPlaying()
    }

    override fun onRepeatModeChanged() {
        updateRepeatMode()
    }

    override fun onShuffleModeChanged() {
        updateShuffleMode()
    }

    override fun onMediaStoreChanged() {
        updateQueue()
        updatePosition()
        updateCurrentSong()
    }
}
