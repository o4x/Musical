package com.o4x.musical.ui.viewmodel

import android.widget.SeekBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.MusicProgressViewUpdateHelper
import com.o4x.musical.util.MusicUtil

class ProgressViewModel() : ViewModel(),
    MusicProgressViewUpdateHelper.Callback,
    SeekBar.OnSeekBarChangeListener {

    private val _progress = MutableLiveData<Int>()
    private val _total = MutableLiveData<Int>()

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
}
