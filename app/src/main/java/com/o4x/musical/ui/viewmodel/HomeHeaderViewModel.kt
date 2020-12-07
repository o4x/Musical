package com.o4x.musical.ui.viewmodel

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.App
import com.o4x.musical.helper.MyPalette
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.imageloader.glide.targets.CustomBitmapTarget
import com.o4x.musical.imageloader.glide.targets.palette.PaletteTargetListener
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.prefs.HomeHeaderPref
import com.o4x.musical.prefs.PreferenceUtil
import com.o4x.musical.repository.SongRepository
import com.o4x.musical.ui.fragments.settings.homehader.defaultImages
import com.o4x.musical.util.CoverUtil
import com.o4x.musical.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeHeaderViewModel(val songRepository: SongRepository) : ViewModel(),
    MusicServiceEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val posterBitmap = MutableLiveData<Bitmap>()
    fun getPosterBitmap(): LiveData<Bitmap> = posterBitmap

    init {
        fetchPosterBitmap()
        HomeHeaderPref.registerOnSharedPreferenceChangedListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        HomeHeaderPref.unregisterOnSharedPreferenceChangedListener(this)
    }

    private fun fetchPosterBitmap() {
        viewModelScope.launch {

            val loader = GlideLoader.with(App.getContext())
                .withListener(object : PaletteTargetListener(App.getContext()) {
                    override fun onColorReady(colors: MyPalette, resource: Bitmap?) {
                        if (resource == null) return

                        val bitmap = if (PreferenceUtil.isDarkMode ==
                            ColorUtil.isColorDark(colors.backgroundColor)
                        ) {
                            CoverUtil.addGradientTo(resource)
                        } else {
                            CoverUtil.doubleGradient(
                                colors.backgroundColor,
                                colors.mightyColor
                            )
                        }

                        posterBitmap.postValue(bitmap)
                    }
                })

            var finisher: GlideLoader.GlideBuilder.GlideFinisher? = null

            viewModelScope.launch(Dispatchers.IO) {
                when (HomeHeaderPref.homeHeaderType) {
                    HomeHeaderPref.TYPE_CUSTOM -> {
                        val uri = Uri.parse(HomeHeaderPref.customImagePath)
                        finisher = loader
                            .load(uri)
                    }
                    HomeHeaderPref.TYPE_TAG -> {
                        val song =
                            songRepository.song(HomeHeaderPref.imageSongID)
                        finisher = loader
                            .load(song)
                    }
                    HomeHeaderPref.TYPE_DEFAULT -> {
                        finisher = loader
                            .load(defaultImages[HomeHeaderPref.defaultImageIndex])
                    }
                }

                withContext(Dispatchers.Main) {
                    finisher?.into(
                        CustomBitmapTarget(
                            Util.getMaxScreenSize(), Util.getMaxScreenSize()
                        )
                    )
                }
            }
        }
    }

    override fun onServiceConnected() {}

    override fun onServiceDisconnected() {}

    override fun onQueueChanged() {}

    override fun onPlayingMetaChanged() {}

    override fun onPlayStateChanged() {}

    override fun onRepeatModeChanged() {}

    override fun onShuffleModeChanged() {}

    override fun onMediaStoreChanged() {
        fetchPosterBitmap()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
            fetchPosterBitmap()
        }
    }

}