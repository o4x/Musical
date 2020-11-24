package com.o4x.musical.ui.viewmodel

import android.widget.SeekBar
import androidx.lifecycle.*
import com.o4x.musical.App
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.MusicProgressViewUpdateHelper
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.imageloader.glide.targets.CustomBitmapTarget
import com.o4x.musical.imageloader.glide.targets.palette.NotificationPaletteTargetListener
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.model.Song
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.color.MediaNotificationProcessor

class PlayerViewModel : ViewModel(),
    MusicProgressViewUpdateHelper.Callback,
    SeekBar.OnSeekBarChangeListener, MusicServiceEventListener {

      ///////////////////
     // PROGRESS PART //
    ///////////////////

    private val _progress = MutableLiveData<Int>()
    private val _total = MutableLiveData<Int>()

    private fun updateTimes() {
        _progress.postValue(MusicPlayerRemote.position)
        _total.postValue(MusicPlayerRemote.songDurationMillis)
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        _progress.postValue(progress)
        _total.postValue(total)
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
        _isPlaying.postValue(MusicPlayerRemote.isPlaying)
    }

    private val _repeatMode = MutableLiveData<Int>()
    val repeatMode: LiveData<Int> = _repeatMode
    private fun updateRepeatMode() {
        _repeatMode.postValue(MusicPlayerRemote.repeatMode)
    }

    private val _shuffleMode = MutableLiveData<Int>()
    val shuffleMode: LiveData<Int> = _shuffleMode
    private fun updateShuffleMode() {
        _shuffleMode.postValue(MusicPlayerRemote.shuffleMode)
    }

      ///////////
     // QUEUE //
    ///////////

    private val _queue = MutableLiveData<List<Song>>()
    val queue: LiveData<List<Song>> = _queue
    private fun updateQueue() {
        _queue.postValue(MusicPlayerRemote.playingQueue)
    }

    private val _position = MutableLiveData<Int>()
    val position: LiveData<Int> = _position
    private fun updatePosition() {
        MusicPlayerRemote.position.let {
            if (it >= 0) {
                _position.postValue(it)
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
        _currentSong.postValue(MusicPlayerRemote.currentSong)
    }

      //////////
     // INIT //
    //////////

    private val progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)

    init {
        progressViewUpdateHelper.start()

        updateAll()
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
        updateAll()
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
        updateCurrentSong()
    }
}
