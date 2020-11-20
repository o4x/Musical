package com.o4x.musical.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.o4x.musical.helper.MusicProgressViewUpdateHelper

class ProgressViewModel() : ViewModel(), MusicProgressViewUpdateHelper.Callback {

    private val _progress = MutableLiveData<Int>()
    private val _total = MutableLiveData<Int>()

    val progress: LiveData<Int> = _progress
    val total: LiveData<Int> = _total

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
}