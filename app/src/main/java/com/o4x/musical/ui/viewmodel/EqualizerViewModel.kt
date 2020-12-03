package com.o4x.musical.ui.viewmodel

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import androidx.lifecycle.ViewModel
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.model.EqualizerModel
import com.o4x.musical.prefs.EqualizerPref

class EqualizerViewModel : ViewModel() {

    val equalizer: Equalizer by lazy { MusicPlayerRemote.equalizer }
    val bassBoost: BassBoost by lazy { MusicPlayerRemote.bassBoost }
    val presetReverb: PresetReverb by lazy { MusicPlayerRemote.presetReverb }


    var isEqualizerReloaded = true
    var em: EqualizerModel = EqualizerModel().apply {
        reverbPreset = PresetReverb.PRESET_NONE
        bassStrength = (1000 / 19).toShort()
    }

    init {
        bassBoost.enabled = EqualizerPref.isEqualizerEnabled

        val bassBoostSettingTemp = bassBoost.properties
        val bassBoostSetting = BassBoost.Settings(bassBoostSettingTemp.toString())
        bassBoostSetting.strength = em.bassStrength

        bassBoost.properties = bassBoostSetting
        presetReverb.preset = em.reverbPreset
        presetReverb.enabled = EqualizerPref.isEqualizerEnabled
        equalizer.enabled = EqualizerPref.isEqualizerEnabled

        if (em.presetPos == 0) {
            for (bandIdx in 0 until equalizer.numberOfBands) {
                equalizer.setBandLevel(
                    bandIdx.toShort(), em.seekbarpos[bandIdx]
                        .toShort()
                )
            }
        } else {
            equalizer.usePreset(em.presetPos.toShort())
        }
    }

    fun updateEq() {

    }

}