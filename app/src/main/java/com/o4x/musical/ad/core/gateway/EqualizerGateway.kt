package com.o4x.musical.ad.core.gateway

import com.o4x.musical.ad.core.entity.EqualizerPreset
import kotlinx.coroutines.flow.Flow

interface EqualizerGateway {

    fun getPresets(): List<EqualizerPreset>
    fun getCurrentPreset(): EqualizerPreset
    fun observeCurrentPreset(): Flow<EqualizerPreset>

    suspend fun addPreset(preset: EqualizerPreset)
    suspend fun updatePreset(preset: EqualizerPreset)
    suspend fun deletePreset(preset: EqualizerPreset)

}