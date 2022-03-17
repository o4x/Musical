package github.o4x.musical.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScrollPositionViewModel : ViewModel() {

    private val position: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            it.value = 0
        }
    }

    fun getPosition(): LiveData<Int> {
        return position
    }

    fun getPositionValue(): Int {
        return position.value ?: 0
    }

    fun setPosition(value: Int) {
        position.value = value
    }

    fun addPosition(value: Int) {
        position.value = getPositionValue() + value
    }
}