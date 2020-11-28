package com.o4x.musical.ui.viewmodel

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import androidx.lifecycle.ViewModel
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.model.EqualizerModel

class EqualizerViewModel : ViewModel() {

    val equalizer: Equalizer by lazy { MusicPlayerRemote.equalizer }
    val bassBoost: BassBoost by lazy { MusicPlayerRemote.bassBoost }
    val presetReverb: PresetReverb by lazy { MusicPlayerRemote.presetReverb }

    var isEqualizerEnabled = true
    var isEqualizerReloaded = true
    var seekbarpos = IntArray(5)
    var presetPos = 0
    var reverbPreset: Short = -1
    var bassStrength: Short = -1
    var equalizerModel: EqualizerModel = EqualizerModel().apply {
        reverbPreset = PresetReverb.PRESET_NONE
        bassStrength = (1000 / 19).toShort()
    }
    var ratio = 1.0

    init {
        bassBoost.enabled = isEqualizerEnabled

        val bassBoostSettingTemp = bassBoost.properties
        val bassBoostSetting = BassBoost.Settings(bassBoostSettingTemp.toString())
        bassBoostSetting.strength = equalizerModel.bassStrength

        bassBoost.properties = bassBoostSetting
        presetReverb.preset = equalizerModel.reverbPreset
        presetReverb.enabled = isEqualizerEnabled
        equalizer.enabled = isEqualizerEnabled

        if (presetPos == 0) {
            for (bandIdx in 0 until equalizer.numberOfBands) {
                equalizer.setBandLevel(
                    bandIdx.toShort(), seekbarpos[bandIdx]
                        .toShort()
                )
            }
        } else {
            equalizer.usePreset(presetPos.toShort())
        }
    }


}