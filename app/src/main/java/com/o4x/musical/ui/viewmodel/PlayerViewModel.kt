package com.o4x.musical.ui.viewmodel

import android.widget.SeekBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
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

class PlayerViewModel() : ViewModel(),
    MusicProgressViewUpdateHelper.Callback,
    SeekBar.OnSeekBarChangeListener, MusicServiceEventListener {

      ///////////////////
     // PROGRESS PART //
    ///////////////////

    private val _progress = MutableLiveData<Int>().also {
        it.value = MusicPlayerRemote.position
    }
    private val _total = MutableLiveData<Int>().also {
        it.value = MusicPlayerRemote.songDurationMillis
    }

    private val progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)

    init {
        progressViewUpdateHelper.start()
    }

    override fun onCleared() {
        progressViewUpdateHelper.stop()
        super.onCleared()
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

    private val _repeatMode = MutableLiveData<Int>().also {
        it.value = MusicPlayerRemote.repeatMode
    }
    val repeatMode: LiveData<Int> = _repeatMode
    private fun updateRepeatMode() {
        _repeatMode.postValue(MusicPlayerRemote.repeatMode)
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


    val _currentPalette = MutableLiveData<MediaNotificationProcessor>().apply {
        currentSong.observeForever {
            val listener: NotificationPaletteTargetListener =
                object : NotificationPaletteTargetListener(App.getContext()) {
                    override fun onColorReady(colors: MediaNotificationProcessor) {
                        postValue(colors)
                    }
                }

            listener.loadPlaceholderPalette = true

            GlideLoader.with(App.getContext())
                .withListener(listener)
                .load(it)
                .into(CustomBitmapTarget(100, 100))
        }
    }
    val currentPalette: LiveData<MediaNotificationProcessor> = _currentPalette

      ///////////////////////
     // SERVICE CALLBACKS //
    ///////////////////////

    override fun onServiceConnected() {
        updateRepeatMode()
        updateCurrentSong()
    }

    override fun onServiceDisconnected() {}

    override fun onQueueChanged() {}

    override fun onPlayingMetaChanged() {
        updateCurrentSong()
    }

    override fun onPlayStateChanged() {}

    override fun onRepeatModeChanged() {
        updateRepeatMode()
    }

    override fun onShuffleModeChanged() {}

    override fun onMediaStoreChanged() {
        updateCurrentSong()
    }
}
