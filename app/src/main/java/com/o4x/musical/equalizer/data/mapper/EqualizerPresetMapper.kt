package com.o4x.musical.equalizer.data.mapper

import com.o4x.musical.equalizer.core.entity.EqualizerBand
import com.o4x.musical.equalizer.core.entity.EqualizerPreset
import com.o4x.musical.equalizer.data.model.db.EqualizerBandEntity
import com.o4x.musical.equalizer.data.model.db.EqualizerPresetEntity

internal fun EqualizerPresetEntity.toDomain(): EqualizerPreset {
    return EqualizerPreset(
        id = id,
        name = name,
        bands = bands.map {
            EqualizerBand(gain = it.gain, frequency = it.frequency)
        },
        isCustom = isCustom
    )
}

internal fun EqualizerPreset.toEntity(): EqualizerPresetEntity {
    return EqualizerPresetEntity(
        id = id,
        name = name,
        bands = bands.map {
            EqualizerBandEntity(gain = it.gain, frequency = it.frequency)
        },
        isCustom = isCustom
    )
}