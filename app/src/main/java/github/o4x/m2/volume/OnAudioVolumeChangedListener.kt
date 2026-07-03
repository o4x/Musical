package github.o4x.m2.volume

interface OnAudioVolumeChangedListener {
    fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int)
}