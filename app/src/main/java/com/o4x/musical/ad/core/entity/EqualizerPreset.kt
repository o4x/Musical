package com.o4x.musical.ad.core.entity

data class EqualizerPreset(
    val id: Long,
    val name: String,
    val bands: List<EqualizerBand>,
    val isCustom: Boolean
)

data class EqualizerBand(
    val gain: Float,
    val frequency: Float
) {

    val displayableGain: String
        get() {
            val gain = this.gain.toInt()
            var str = when {
                gain > 0 -> "+$gain"
                gain < 0 -> "$gain"
                else -> gain.toString()
            }
            str += "\ndB"
            return str
        }

    val displayableFrequency: String
        get() {
            var freq = frequency.toInt().toString()
            if (frequency >= 1000) {
                freq = "${freq.dropLast(3)}\nK"
            } else {
                freq += "\n"
            }
            freq += "Hz"
            return freq
        }

}