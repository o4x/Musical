package com.o4x.musical.ui.fragments.settings.homehader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentHomeHeaderBinding
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.prefs.HomeHeaderPref
import com.o4x.musical.ui.activities.MusicPickerActivity
import com.o4x.musical.ui.viewmodel.HomeHeaderViewModel
import com.o4x.musical.util.MusicUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class HomeHeaderFragment : Fragment(R.layout.fragment_home_header) {

    companion object {
        const val REQUEST_CODE_SELECT_IMAGE = 1600
        const val REQUEST_CODE_SELECT_SONG = 1700
    }

    private val viewModel by viewModel<HomeHeaderViewModel>()

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

        viewModel.getPosterBitmap().observe(viewLifecycleOwner, {
            GlideApp.with(this)
                .asBitmap()
                .load(it)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(binding.image)
        })

        binding.apply {
            useCustom.setOnClickListener {
                startHomeHeaderImagePicker()
            }
            useSong.setOnClickListener {
                val myIntent = Intent(
                    requireContext(),
                    MusicPickerActivity::class.java
                )
                startActivityForResult(myIntent, REQUEST_CODE_SELECT_SONG)
            }

            useDefault.setOnClickListener {
                HomeHeaderPref.setDefault()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SELECT_IMAGE -> if (resultCode == Activity.RESULT_OK) {
                data?.data?.let { it ->
                    GlobalScope.launch(Dispatchers.IO) {
                        if (headerDir.isDirectory) {
                            val newImage = it.toFile()
                            headerDir.listFiles()?.let {
                                for (image in it) {
                                    if (image != newImage) image.delete()
                                }
                            }
                        }
                    }

                    HomeHeaderPref.customImagePath = it.toString()
                }
            }
            REQUEST_CODE_SELECT_SONG -> if (resultCode == Activity.RESULT_OK) {
                data?.data?.let {
                    HomeHeaderPref.imageSongID = MusicUtil.getSongIDFromFileUri(it)
                }
            }
        }
    }

    private val headerDir = File(App.getContext().filesDir, "/home_header/")

    private fun startHomeHeaderImagePicker() {
        ImagePicker.with(this)
            .saveDir(headerDir)
            .crop()
            .start(REQUEST_CODE_SELECT_IMAGE)
    }
}