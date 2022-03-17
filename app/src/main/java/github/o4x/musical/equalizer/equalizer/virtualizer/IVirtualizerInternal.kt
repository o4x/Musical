package github.o4x.musical.equalizer.equalizer.virtualizer

internal interface IVirtualizerInternal {

    fun getStrength(): Int
    fun setStrength(value: Int)

    fun onAudioSessionIdChanged(audioSessionId: Int)

    fun setEnabled(enabled: Boolean)

    fun onDestroy()

}