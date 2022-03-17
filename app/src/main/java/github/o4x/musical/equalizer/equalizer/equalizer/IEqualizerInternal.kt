package github.o4x.musical.equalizer.equalizer.equalizer

import github.o4x.musical.equalizer.core.entity.EqualizerBand
import github.o4x.musical.equalizer.core.entity.EqualizerPreset
import kotlinx.coroutines.flow.Flow

internal interface IEqualizerInternal {

    fun onAudioSessionIdChanged(audioSessionId: Int)
    fun onDestroy()
    fun setEnabled(enabled: Boolean)

    fun getPresets(): List<EqualizerPreset>
    fun observeCurrentPreset(): Flow<EqualizerPreset>
    fun getCurrentPreset(): EqualizerPreset
    suspend fun setCurrentPreset(preset: EqualizerPreset)
    suspend fun updateCurrentPresetIfCustom()

    fun getBandCount(): Int
    fun getBandLevel(band: Int): Float
    fun getAllBandsCurrentLevel(): List<EqualizerBand>
    fun setBandLevel(band: Int, level: Float)
    fun getBandLimit(): Float

}