package com.o4x.musical.ui.fragments.mainactivity.equalizer

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import code.name.monkey.appthemehelper.extensions.primaryColor
import com.db.williamchart.data.AxisType
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentEqualizerBinding
import com.o4x.musical.helper.MusicPlayerRemote.audioSessionId
import com.o4x.musical.model.EqualizerModel
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import java.util.*


class EqualizerFragment : AbsMainActivityFragment(R.layout.fragment_equalizer) {

    companion object {
        var showBackButton = true
    }

    private val themeColor by lazy { primaryColor() }

    var dataSet = mutableListOf<Pair<String, Float>>()

    private val numberOfFrequencyBands: Short = 5
    val points: FloatArray = FloatArray(numberOfFrequencyBands.toInt())

    var y = 0

    var seekBarFinal = arrayOfNulls<SeekBar>(5)

    val mEqualizer: Equalizer by lazy { Equalizer(0, audioSesionId) }
    private val bassBoost: BassBoost by lazy { BassBoost(0, audioSesionId) }
    private lateinit var presetReverb: PresetReverb

    private var audioSesionId = 0

    private var _binding: FragmentEqualizerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEqualizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAppbarPadding(view)

        Settings.isEditing = true

        audioSesionId = audioSessionId
        if (Settings.equalizerModel == null) {
            Settings.equalizerModel = EqualizerModel()
            Settings.equalizerModel.reverbPreset = PresetReverb.PRESET_NONE
            Settings.equalizerModel.bassStrength = (1000 / 19).toShort()
        }


        bassBoost.enabled = Settings.isEqualizerEnabled
        val bassBoostSettingTemp = bassBoost.properties
        val bassBoostSetting = BassBoost.Settings(bassBoostSettingTemp.toString())
        bassBoostSetting.strength = Settings.equalizerModel.bassStrength
        bassBoost.properties = bassBoostSetting
        presetReverb = PresetReverb(0, audioSesionId)
        presetReverb.preset = Settings.equalizerModel.reverbPreset
        presetReverb.enabled = Settings.isEqualizerEnabled
        mEqualizer.enabled = Settings.isEqualizerEnabled
        if (Settings.presetPos == 0) {
            for (bandIdx in 0 until mEqualizer.numberOfBands) {
                mEqualizer.setBandLevel(
                    bandIdx.toShort(), Settings.seekbarpos[bandIdx]
                        .toShort()
                )
            }
        } else {
            mEqualizer.usePreset(Settings.presetPos.toShort())
        }

        binding.spinnerDropdownIcon.setOnClickListener { binding.presetSpinner.performClick() }
        binding.controllerBass.label = "BASS"
        binding.controller3D.label = "3D"
        binding.controllerBass.circlePaint2.color = themeColor
        binding.controllerBass.linePaint.color = themeColor
        binding.controllerBass.invalidate()
        binding.controller3D.circlePaint2.color = themeColor
        binding.controllerBass.linePaint.color = themeColor
        binding.controller3D.invalidate()
        if (!Settings.isEqualizerReloaded) {
            var x = 0
            try {
                x = bassBoost.roundedStrength * 19 / 1000
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                y = presetReverb.preset * 19 / 6
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (x == 0) {
                binding.controllerBass.progress = 1
            } else {
                binding.controllerBass.progress = x
            }
            if (y == 0) {
                binding.controller3D.progress = 1
            } else {
                binding.controller3D.progress = y
            }
        } else {
            val x = Settings.bassStrength * 19 / 1000
            y = Settings.reverbPreset * 19 / 6
            if (x == 0) {
                binding.controllerBass.progress = 1
            } else {
                binding.controllerBass.progress = x
            }
            if (y == 0) {
                binding.controller3D.progress = 1
            } else {
                binding.controller3D.progress = y
            }
        }
        binding.controllerBass.setOnProgressChangedListener { progress ->
            Settings.bassStrength = (1000.toFloat() / 19 * progress).toInt().toShort()
            try {
                bassBoost.setStrength(Settings.bassStrength)
                Settings.equalizerModel.bassStrength = Settings.bassStrength
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.controller3D.setOnProgressChangedListener { progress ->
            Settings.reverbPreset = (progress * 6 / 19).toShort()
            Settings.equalizerModel.reverbPreset = Settings.reverbPreset
            try {
                presetReverb.preset = Settings.reverbPreset
            } catch (e: Exception) {
                e.printStackTrace()
            }
            y = progress
        }
        val equalizerHeading = TextView(context)
        equalizerHeading.setText(R.string.eq)
        equalizerHeading.textSize = 20f
        equalizerHeading.gravity = Gravity.CENTER_HORIZONTAL
        val lowerEqualizerBandLevel = mEqualizer.bandLevelRange[0]
        val upperEqualizerBandLevel = mEqualizer.bandLevelRange[1]
        for (i in 0 until numberOfFrequencyBands) {
            val equalizerBandIndex = i.toShort()
            val frequencyHeaderTextView = TextView(context)
            frequencyHeaderTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            frequencyHeaderTextView.gravity = Gravity.CENTER_HORIZONTAL
            frequencyHeaderTextView.setTextColor(Color.parseColor("#FFFFFF"))
            frequencyHeaderTextView.text =
                (mEqualizer.getCenterFreq(equalizerBandIndex) / 1000).toString() + "Hz"
            val seekBarRowLayout = LinearLayout(context)
            seekBarRowLayout.orientation = LinearLayout.VERTICAL
            val lowerEqualizerBandLevelTextView = TextView(context)
            lowerEqualizerBandLevelTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            lowerEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"))
            lowerEqualizerBandLevelTextView.text = (lowerEqualizerBandLevel / 100).toString() + "dB"
            val upperEqualizerBandLevelTextView = TextView(context)
            lowerEqualizerBandLevelTextView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            upperEqualizerBandLevelTextView.setTextColor(Color.parseColor("#FFFFFF"))
            upperEqualizerBandLevelTextView.text = (upperEqualizerBandLevel / 100).toString() + "dB"
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.weight = 1f
            var seekBar = SeekBar(context)
            var textView = TextView(context)
            when (i) {
                0 -> {
                    seekBar = binding.seekBar1
                    textView = binding.text1
                }
                1 -> {
                    seekBar = binding.seekBar2
                    textView = binding.text2
                }
                2 -> {
                    seekBar = binding.seekBar3
                    textView = binding.text3
                }
                3 -> {
                    seekBar = binding.seekBar4
                    textView = binding.text4
                }
                4 -> {
                    seekBar = binding.seekBar5
                    textView = binding.text5
                }
            }
            seekBarFinal[i] = seekBar
            seekBar.progressDrawable.colorFilter =
                PorterDuffColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN)
            seekBar.thumb.colorFilter = PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN)
            seekBar.id = i
            //            seekBar.setLayoutParams(layoutParams);
            seekBar.max = upperEqualizerBandLevel - lowerEqualizerBandLevel
            textView.text = frequencyHeaderTextView.text
            textView.setTextColor(Color.WHITE)
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            if (Settings.isEqualizerReloaded) {
                points[i] = (Settings.seekbarpos[i] - lowerEqualizerBandLevel).toFloat()
                dataSet.add(frequencyHeaderTextView.text.toString() to points[i])
                seekBar.progress = Settings.seekbarpos[i] - lowerEqualizerBandLevel
            } else {
                points[i] =
                    (mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel).toFloat()
                dataSet.add(frequencyHeaderTextView.text.toString() to points[i])
                seekBar.progress =
                    mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel
                Settings.seekbarpos[i] = mEqualizer.getBandLevel(equalizerBandIndex)
                    .toInt()
                Settings.isEqualizerReloaded = true
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    mEqualizer.setBandLevel(
                        equalizerBandIndex,
                        (progress + lowerEqualizerBandLevel).toShort()
                    )
                    points[seekBar.id] =
                        (mEqualizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel).toFloat()
                    Settings.seekbarpos[seekBar.id] = progress + lowerEqualizerBandLevel
                    Settings.equalizerModel.seekbarpos[seekBar.id] =
                        progress + lowerEqualizerBandLevel
                    dataSet.forEachIndexed { index, pair ->
                        dataSet[index] = pair.copy(second = points[index])
                    }
                    binding.chart.animate(dataSet)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    binding.presetSpinner.setSelection(0)
                    Settings.presetPos = 0
                    Settings.equalizerModel.presetPos = 0
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        equalizeSound()
        binding.chart.lineColor = themeColor
        binding.chart.smooth = true
        binding.chart.lineThickness = 5f
        binding.chart.axis = AxisType.NONE
        binding.chart.show(dataSet)
        val mEndButton = Button(context)
        mEndButton.setBackgroundColor(themeColor)
        mEndButton.setTextColor(Color.WHITE)
    }

    private fun equalizeSound() {
        val equalizerPresetNames = ArrayList<String>()
        val equalizerPresetSpinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            equalizerPresetNames
        )
        equalizerPresetSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        equalizerPresetNames.add("Custom")
        for (i in 0 until mEqualizer.numberOfPresets) {
            equalizerPresetNames.add(mEqualizer.getPresetName(i.toShort()))
        }
        binding.presetSpinner.adapter = equalizerPresetSpinnerAdapter
        //presetSpinner.setDropDownWidth((Settings.screen_width * 3) / 4);
        if (Settings.isEqualizerReloaded && Settings.presetPos != 0) {
//            correctPosition = false;
            binding.presetSpinner.setSelection(Settings.presetPos)
        }
        binding.presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                try {
                    if (position != 0) {
                        mEqualizer.usePreset((position - 1).toShort())
                        Settings.presetPos = position
                        val numberOfFreqBands: Short = 5
                        val lowerEqualizerBandLevel = mEqualizer.bandLevelRange[0]
                        for (i in 0 until numberOfFreqBands) {
                            seekBarFinal[i]!!.progress =
                                mEqualizer.getBandLevel(i.toShort()) - lowerEqualizerBandLevel
                            points[i] =
                                (mEqualizer.getBandLevel(i.toShort()) - lowerEqualizerBandLevel).toFloat()
                            Settings.seekbarpos[i] = mEqualizer.getBandLevel(
                                i.toShort()
                            ).toInt()
                            Settings.equalizerModel.seekbarpos[i] = mEqualizer.getBandLevel(
                                i.toShort()
                            ).toInt()
                        }
                        dataSet.forEachIndexed { index, pair ->
                            dataSet[index] = pair.copy(second = points[index])
                        }
                        binding.chart.animate(dataSet)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error while updating Equalizer", Toast.LENGTH_SHORT)
                        .show()
                }
                Settings.equalizerModel.presetPos = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_equalizer, menu)
        val item = menu.findItem(R.id.equalizer_switch)
        item.setActionView(R.layout.switch_layout)
        val equalizerSwitch: SwitchCompat =
            item.actionView.findViewById(R.id.equalizer_enable_switch)
        equalizerSwitch.isChecked = Settings.isEqualizerEnabled
        equalizerSwitch.setOnCheckedChangeListener { _, isChecked ->
            mEqualizer.enabled = isChecked
            bassBoost.enabled = isChecked
            presetReverb.enabled = isChecked
            Settings.isEqualizerEnabled = isChecked
            Settings.equalizerModel.isEqualizerEnabled = isChecked
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroy() {
        super.onDestroy()
        mEqualizer.release()
        bassBoost.release()
        presetReverb.release()
        Settings.isEditing = false
    }
}