package com.o4x.musical.ui.fragments.mainactivity.equalizer

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import code.name.monkey.appthemehelper.extensions.accentColor
import code.name.monkey.appthemehelper.extensions.backgroundColor
import code.name.monkey.appthemehelper.extensions.primaryColor
import code.name.monkey.appthemehelper.extensions.textColorPrimary
import code.name.monkey.appthemehelper.util.ColorUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentEqualizerBinding
import com.o4x.musical.prefs.PreferenceUtil
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.ui.viewmodel.EqualizerViewModel
import com.sdsmdg.harjot.crollerTest.Croller
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class EqualizerFragment : AbsMainActivityFragment(R.layout.fragment_equalizer), CoroutineScope by MainScope() {

    companion object {
        const val TAG = "EqualizerFragment"
    }

    private val presenter by viewModel<EqualizerViewModel>()

    private val limit by lazy { presenter.getBandLimit().toInt() }

    private var _binding: FragmentEqualizerBinding? = null
    private val binding get() = _binding!!

    private val bands = mutableListOf<VerticalSeekBarLayout>()

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
        presenter.updateCurrentPresetIfCustom()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAppbarPadding(binding.root)

        binding.controllerBass.apply {
            label = "BASS"
            min = 0
            max = 1000
            progress = presenter.getBassStrength()

            applyColor()
        }
        binding.controller3D.apply {
            label = "3D"
            min = 0
            max = 1000
            progress = presenter.getVirtualizerStrength()

            applyColor()
        }

        buildBands()

        presenter.observePreset().observe(viewLifecycleOwner, {
            binding.delete.isVisible = it.isCustom

            binding.presetSpinner.text = it.name

            it.bands.forEachIndexed { index, band ->
                val layout = bands[index]
                layout.seekBar.apply {
                    max = limit * 2
                    this.progress = band.gain.toInt() + limit
                }
                layout.value.text = band.displayableGain
                layout.text.text = band.displayableFrequency
            }
        })
    }

    private fun buildBands() {
        for (band in 0 until presenter.getBandCount()) {
            val view = binding.bands.getChildAt(band)
            view.isVisible = true
            val layout = VerticalSeekBarLayout(view)
            layout.seekBar.apply {
                max = limit * 2
            }
            bands.add(layout)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.controllerBass.setOnProgressChangedListener(onBassKnobChangeListener)
        binding.controller3D.setOnProgressChangedListener(onVirtualizerKnobChangeListener)

        setupBandListeners { band -> BandListener(band) }

        binding.presetSpinner.setOnClickListener { changePreset() }
        binding.delete.setOnClickListener { presenter.deleteCurrentPreset() }
        binding.save.setOnClickListener {
            // create new preset
            MaterialDialog(requireContext())
                .title(R.string.save_as_preset)
                .positiveButton(R.string.create_action)
                .negativeButton(R.string.cancel)
                .input(
                    hintRes = R.string.title,
                    inputType = InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                            InputType.TYPE_TEXT_FLAG_CAP_WORDS
                ) { _, charSequence ->
                    if (activity == null) return@input
                    val name: String = charSequence.toString().trim()
                    if (name.isNotEmpty()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            presenter.addPreset(name)
                        }
                    }
                }.show()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.controllerBass.setOnProgressChangedListener(null)
        binding.controller3D.setOnProgressChangedListener(null)

        setupBandListeners(null)

        binding.presetSpinner.setOnClickListener(null)
        binding.delete.setOnClickListener(null)
        binding.save.setOnClickListener(null)
    }

    private fun changePreset() {
        launch {
            val presets = withContext(Dispatchers.IO) {
                presenter.getPresets()
            }
            val popup = PopupMenu(requireContext(), binding.presetSpinner)
            for (preset in presets) {
                popup.menu.add(Menu.NONE, preset.id.toInt(), Menu.NONE, preset.name)
            }
            popup.setOnMenuItemClickListener { menu ->
                val preset = presets.first { it.id.toInt() == menu.itemId }
                binding.presetSpinner.text = preset.name
                presenter.setCurrentPreset(preset)
                true
            }
            popup.show()
        }
    }

    private fun setupBandListeners(listener: ((Int) -> BandListener)?) {
        bands.forEachIndexed { index, view ->
            view.seekBar.setOnSeekBarChangeListener(listener?.invoke(index))
        }
    }

    inner class BandListener(private val band: Int) : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            presenter.setBandLevel(band, progress.toFloat() - limit)
            bands[band].value.text = presenter.getBandLevel(band).displayableGain
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private val onBassKnobChangeListener =
        Croller.onProgressChangedListener { progress -> presenter.setBassStrength(progress) }

    private val onVirtualizerKnobChangeListener =
        Croller.onProgressChangedListener { progress -> presenter.setVirtualizerStrength(progress) }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_equalizer, menu)
        val item = menu.findItem(R.id.equalizer_switch)
        item.setActionView(R.layout.switch_layout)
        val equalizerSwitch: SwitchCompat =
            item.actionView.findViewById(R.id.equalizer_enable_switch)

        equalizerSwitch.isChecked = presenter.isEqualizerEnabled()
        binding.blocker.isVisible = !equalizerSwitch.isChecked
        equalizerSwitch.setOnCheckedChangeListener { _, isChecked ->

            presenter.setEqualizerEnabled(isChecked)

            binding.blocker.isVisible = !isChecked
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    inner class VerticalSeekBarLayout(view: View) {
        val value = view.findViewById<TextView>(R.id.value)!!
        val seekBar = view.findViewById<VerticalSeekBar>(R.id.seek_bar)!!
        val text = view.findViewById<TextView>(R.id.text)!!
    }

    private fun Croller.applyColor() {
        if (PreferenceUtil.isDarkMode) {
            backCircleColor = ColorUtil.shiftColor(Color.GRAY, .3f)
            mainCircleColor = ColorUtil.shiftColor(backCircleColor, .3f)
        } else {
            mainCircleColor = ColorUtil.shiftColor(backgroundColor(), .9f)
            backCircleColor = ColorUtil.shiftColor(mainCircleColor, .9f)
        }
        labelColor = textColorPrimary()
        progressPrimaryColor = accentColor()
        indicatorColor = accentColor()
        progressSecondaryColor = mainCircleColor
    }
}