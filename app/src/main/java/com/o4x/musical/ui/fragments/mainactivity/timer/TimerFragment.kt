package com.o4x.musical.ui.fragments.mainactivity.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentTimerBinding
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment

class TimerFragment: AbsMainActivityFragment(R.layout.fragment_timer) {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}