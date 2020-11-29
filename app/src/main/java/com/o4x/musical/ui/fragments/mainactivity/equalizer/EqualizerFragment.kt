package com.o4x.musical.ui.fragments.mainactivity.equalizer

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import code.name.monkey.appthemehelper.extensions.primaryColor
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentEqualizerBinding
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.ui.viewmodel.EqualizerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class EqualizerFragment : AbsMainActivityFragment(R.layout.fragment_equalizer) {

    val equalizerViewModel by viewModel<EqualizerViewModel>()

    private val themeColor by lazy { primaryColor() }

    private val numberOfFrequencyBands: Short = 5

    var seekBarFinal = arrayOfNulls<SeekBar>(5)


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

        binding.spinnerDropdownIcon.setOnClickListener { binding.presetSpinner.performClick() }

        binding.controllerBass.label = "BASS"
        binding.controllerBass.circlePaint2.color = themeColor
        binding.controllerBass.linePaint.color = themeColor
        binding.controllerBass.invalidate()

        binding.controller3D.label = "3D"
        binding.controller3D.circlePaint2.color = themeColor
        binding.controller3D.linePaint.color = themeColor
        binding.controller3D.invalidate()

        if (!equalizerViewModel.isEqualizerReloaded) {
            var x = 0
            var y = 0
            try {
                x = equalizerViewModel.bassBoost.roundedStrength * 19 / 1000
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                y = equalizerViewModel.presetReverb.preset * 19 / 1000
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
            val x = equalizerViewModel.bassStrength * 19 / 1000
            val y = equalizerViewModel.reverbPreset * 19 / 1000
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
            equalizerViewModel.bassStrength = (progress * 1000 / 19).toShort()
            try {
                equalizerViewModel.bassBoost.setStrength(equalizerViewModel.bassStrength)
                equalizerViewModel.equalizerModel.bassStrength = equalizerViewModel.bassStrength
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.controller3D.setOnProgressChangedListener { progress ->
            equalizerViewModel.reverbPreset = (progress * 1000 / 19).toShort()
            equalizerViewModel.equalizerModel.reverbPreset = equalizerViewModel.reverbPreset
            try {
                equalizerViewModel.presetReverb.preset = equalizerViewModel.reverbPreset
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val equalizerHeading = TextView(context)
        equalizerHeading.setText(R.string.eq)
        equalizerHeading.textSize = 20f
        equalizerHeading.gravity = Gravity.CENTER_HORIZONTAL
        val lowerEqualizerBandLevel = equalizerViewModel.equalizer.bandLevelRange[0]
        val upperEqualizerBandLevel = equalizerViewModel.equalizer.bandLevelRange[1]
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
                (equalizerViewModel.equalizer.getCenterFreq(equalizerBandIndex) / 1000).toString() + "Hz"
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
                    seekBar = binding.vertical0.seekBar
                    textView = binding.vertical0.text
                }
                1 -> {
                    seekBar = binding.vertical1.seekBar
                    textView = binding.vertical1.text
                }
                2 -> {
                    seekBar = binding.vertical2.seekBar
                    textView = binding.vertical2.text
                }
                3 -> {
                    seekBar = binding.vertical3.seekBar
                    textView = binding.vertical3.text
                }
                4 -> {
                    seekBar = binding.vertical4.seekBar
                    textView = binding.vertical4.text
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
            if (equalizerViewModel.isEqualizerReloaded) {
                seekBar.progress = equalizerViewModel.seekbarpos[i] - lowerEqualizerBandLevel
            } else {
                seekBar.progress =
                    equalizerViewModel.equalizer.getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel
                equalizerViewModel.seekbarpos[i] = equalizerViewModel.equalizer.getBandLevel(equalizerBandIndex)
                    .toInt()
                equalizerViewModel.isEqualizerReloaded = true
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    equalizerViewModel.equalizer.setBandLevel(
                        equalizerBandIndex,
                        (progress + lowerEqualizerBandLevel).toShort()
                    )
                    equalizerViewModel.seekbarpos[seekBar.id] = progress + lowerEqualizerBandLevel
                    equalizerViewModel.equalizerModel.seekbarpos[seekBar.id] =
                        progress + lowerEqualizerBandLevel
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    binding.presetSpinner.setSelection(0)
                    equalizerViewModel.presetPos = 0
                    equalizerViewModel.equalizerModel.presetPos = 0
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        equalizeSound()
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
        for (i in 0 until equalizerViewModel.equalizer.numberOfPresets) {
            equalizerPresetNames.add(equalizerViewModel.equalizer.getPresetName(i.toShort()))
        }
        binding.presetSpinner.adapter = equalizerPresetSpinnerAdapter
        //presetSpinner.setDropDownWidth((Settings.screen_width * 3) / 4);
        if (equalizerViewModel.isEqualizerReloaded && equalizerViewModel.presetPos != 0) {
//            correctPosition = false;
            binding.presetSpinner.setSelection(equalizerViewModel.presetPos)
        }
        binding.presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                try {
                    if (position != 0) {
                        equalizerViewModel.equalizer.usePreset((position - 1).toShort())
                        equalizerViewModel.presetPos = position
                        val numberOfFreqBands: Short = 5
                        val lowerEqualizerBandLevel = equalizerViewModel.equalizer.bandLevelRange[0]
                        for (i in 0 until numberOfFreqBands) {
                            seekBarFinal[i]!!.progress =
                                equalizerViewModel.equalizer.getBandLevel(i.toShort()) - lowerEqualizerBandLevel
                            equalizerViewModel.seekbarpos[i] = equalizerViewModel.equalizer.getBandLevel(
                                i.toShort()
                            ).toInt()
                            equalizerViewModel.equalizerModel.seekbarpos[i] = equalizerViewModel
                                .equalizer.getBandLevel(
                                    i.toShort()
                                ).toInt()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error while updating Equalizer", Toast.LENGTH_SHORT)
                        .show()
                }
                equalizerViewModel.equalizerModel.presetPos = position
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
        equalizerSwitch.isChecked = equalizerViewModel.isEqualizerEnabled
        equalizerSwitch.setOnCheckedChangeListener { _, isChecked ->
            equalizerViewModel.equalizer.enabled = isChecked
            equalizerViewModel.bassBoost.enabled = isChecked
            equalizerViewModel.presetReverb.enabled = isChecked
            equalizerViewModel.isEqualizerEnabled = isChecked
            equalizerViewModel.equalizerModel.isEqualizerEnabled = isChecked

            binding.blocker.isVisible = !isChecked
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
}