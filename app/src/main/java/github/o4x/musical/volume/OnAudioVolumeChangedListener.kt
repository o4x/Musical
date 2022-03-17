package github.o4x.musical.volume

interface OnAudioVolumeChangedListener {
    fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int)
}