package com.o4x.musical.equalizer.data.repository
import com.o4x.musical.equalizer.core.entity.EqualizerPreset
import com.o4x.musical.equalizer.core.gateway.EqualizerGateway
import com.o4x.musical.equalizer.core.prefs.EqualizerPreferencesGateway
import com.o4x.musical.equalizer.data.db.EqualizerPresetsDao
import com.o4x.musical.equalizer.data.mapper.toDomain
import com.o4x.musical.equalizer.data.mapper.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class EqualizerRepository constructor(
    private val equalizerDao: EqualizerPresetsDao,
    private val prefs: EqualizerPreferencesGateway
) : EqualizerGateway {

    init {
        GlobalScope.launch(Dispatchers.IO) {
            if (equalizerDao.getPresets().isEmpty()) {
                // called only first time
                val presets = EqualizerDefaultPresets.createDefaultPresets()
                GlobalScope.launch(Dispatchers.IO) { equalizerDao.insertPresets(*presets.toTypedArray()) }
            }
        }
    }

    override fun getPresets(): List<EqualizerPreset> {
        return equalizerDao.getPresets().map { it.toDomain() }
    }

    override fun getCurrentPreset(): EqualizerPreset {
        val currentPresetId = prefs.getCurrentPresetId()
        return equalizerDao.getPresetById(currentPresetId)!!.toDomain()
    }

    override fun observeCurrentPreset(): Flow<EqualizerPreset> {
        return prefs.observeCurrentPresetId()
            .flatMapLatest { equalizerDao.observePresetById(it) }
            .map { it.toDomain() }
            .distinctUntilChanged()
    }

    override suspend fun addPreset(preset: EqualizerPreset) {
        require(preset.id == -1L)
        require(preset.isCustom)

        val newId = getPresets().maxBy { it.id }!!.id + 1
        equalizerDao.insertPresets(preset.toEntity().copy(id = newId))
    }

    override suspend fun updatePreset(preset: EqualizerPreset) {
        equalizerDao.insertPresets(preset.toEntity())
    }

    override suspend fun deletePreset(preset: EqualizerPreset) {
        equalizerDao.deletePreset(preset.toEntity())
    }

}