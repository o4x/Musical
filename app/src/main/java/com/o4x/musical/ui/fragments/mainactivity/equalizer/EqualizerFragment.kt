package com.o4x.musical.ui.fragments.mainactivity.equalizer

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SwitchCompat
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentEqualizerBinding
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment


class EqualizerFragment : AbsMainActivityFragment(R.layout.fragment_equalizer) {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAppbarPadding(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_equalizer, menu)
        val item = menu.findItem(R.id.equalizer_switch)
        item.setActionView(R.layout.switch_layout)
        val equalizerSwitch: SwitchCompat =
            item.actionView.findViewById(R.id.equalizer_enable_switch)
        equalizerSwitch.setOnCheckedChangeListener { _, isChecked ->
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
}