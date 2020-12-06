package com.o4x.musical.ui.fragments.settings.homehader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentHomeHeaderBinding
import com.o4x.musical.extensions.startHomeHeaderImagePicker
import com.o4x.musical.prefs.HomeHeaderPref
import com.o4x.musical.ui.fragments.mainactivity.datails.AbsDetailFragment
import com.o4x.musical.util.CustomImageUtil

class HomeHeaderFragment : Fragment(R.layout.fragment_home_header) {

    companion object {
        const val REQUEST_CODE_SELECT_IMAGE = 1400
    }

    private var _binding: FragmentHomeHeaderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeHeaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (HomeHeaderPref.homeHeaderType) {
            HomeHeaderPref.TYPE_CUSTOM -> {
                binding.radioCustom.isChecked = true
            }
            HomeHeaderPref.TYPE_TAG -> {
                binding.radioTag.isChecked = true
            }
            HomeHeaderPref.TYPE_DEFAULT -> {
                binding.radioDefault.isChecked = true
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioCustom.id -> {
                    HomeHeaderPref.homeHeaderType = HomeHeaderPref.TYPE_CUSTOM
                    startHomeHeaderImagePicker(REQUEST_CODE_SELECT_IMAGE)
                }
                binding.radioTag.id -> {
                    HomeHeaderPref.homeHeaderType = HomeHeaderPref.TYPE_TAG
                }
                binding.radioDefault.id -> {
                    HomeHeaderPref.homeHeaderType = HomeHeaderPref.TYPE_DEFAULT
                }
            }
        }

        binding.recyclerView.adapter = DefaultImageRecyclerView()
        binding.recyclerView.layoutManager = object: LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false) {

            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                lp.width = (width / 3) - (lp.marginStart * 2 /* for left and right */)
                lp.height = (lp.width * 1.5).toInt()
                return super.checkLayoutParams(lp)
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AbsDetailFragment.REQUEST_CODE_SELECT_IMAGE -> if (resultCode == Activity.RESULT_OK) {
                data?.data?.let {
                    HomeHeaderPref.customImagePath = it.toString()
                }
            }
        }
    }
}