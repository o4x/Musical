package github.o4x.musical.equalizer.equalizer.equalizer

import github.o4x.musical.equalizer.core.entity.EqualizerPreset
import github.o4x.musical.equalizer.core.gateway.EqualizerGateway
import github.o4x.musical.equalizer.core.prefs.EqualizerPreferencesGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

abstract class AbsEqualizer(
    protected val gateway: EqualizerGateway,
    protected val prefs: EqualizerPreferencesGateway
) : IEqualizerInternal {

    override fun getPresets(): List<EqualizerPreset> = gateway.getPresets()

    override fun observeCurrentPreset(): Flow<EqualizerPreset> {
        return gateway.observeCurrentPreset()
    }

    override fun getCurrentPreset(): EqualizerPreset {
        return gateway.getCurrentPreset()
    }

    override suspend fun updateCurrentPresetIfCustom() = withContext(Dispatchers.IO) {
        var preset = gateway.getCurrentPreset()
        if (preset.isCustom) {
            preset = preset.copy(
                bands = getAllBandsCurrentLevel()
            )
            gateway.updatePreset(preset)
        }
    }

}