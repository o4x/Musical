package github.o4x.m2.ui.viewmodel

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import github.o4x.m2.App
import github.o4x.m2.R
import github.o4x.m2.helper.MyPalette
import github.o4x.m2.imageloader.glide.loader.GlideLoader
import github.o4x.m2.imageloader.glide.module.GlideApp
import github.o4x.m2.imageloader.glide.targets.CustomBitmapTarget
import github.o4x.m2.imageloader.glide.targets.palette.PaletteTargetListener
import github.o4x.m2.interfaces.MusicServiceEventListener
import github.o4x.m2.prefs.PreferenceUtil
import github.o4x.m2.util.ColorUtil
import github.o4x.m2.util.CoverUtil
import github.o4x.m2.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeHeaderViewModel : ViewModel(),
    MusicServiceEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val posterBitmap = MutableLiveData<Bitmap>()
    fun getPosterBitmap(): LiveData<Bitmap> = posterBitmap

    init {
        fetchPosterBitmap()
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    private fun fetchPosterBitmap() {
        val listener = object : PaletteTargetListener(App.getContext()) {
            override fun onColorReady(colors: MyPalette, resource: Bitmap?) {
                if (resource == null) return
                posterBitmap.postValue(resource)
            }
        }

        GlideLoader.with(App.getContext())
            .withListener(listener)
            .load(R.drawable.unsplash)
            .into(
                CustomBitmapTarget(
                    Util.getMaxScreenSize(), Util.getMaxScreenSize()
                )
            )
    }

    fun calculateBitmap(image: ImageView, it: Bitmap, w: Int, h: Int) {
        if (w <= 0 || h <= 0)
            return
        viewModelScope.launch(Dispatchers.Default) {
            val paletteBuilder = Palette.from(it)
            val colors = MyPalette(
                image.context,
                paletteBuilder.generate()
            )
            var bitmap = Bitmap.createScaledBitmap(it, w, h, true)
            bitmap = if (PreferenceUtil.isDarkMode ==
                ColorUtil.isColorDark(colors.backgroundColor)
            ) {
                CoverUtil.addGradientTo(bitmap)
            } else {
                CoverUtil.doubleGradient(
                    colors.backgroundColor,
                    colors.mightyColor,
                    w,
                    h
                )
            }

            withContext(Dispatchers.Main) {
                GlideApp.with(image.context)
                    .asBitmap()
                    .load(bitmap)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .into(image)
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

    override fun onMediaStoreChanged() {}

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceUtil.DARK_MODE -> {
                fetchPosterBitmap()
            }
        }
    }

}
