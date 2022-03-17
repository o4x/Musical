package github.o4x.musical.ui.viewmodel

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import com.o4x.appthemehelper.util.ColorUtil
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import github.o4x.musical.App
import github.o4x.musical.drawables.CharCoverDrawable
import github.o4x.musical.helper.MyPalette
import github.o4x.musical.imageloader.glide.loader.GlideLoader
import github.o4x.musical.imageloader.glide.module.GlideApp
import github.o4x.musical.imageloader.glide.targets.CustomBitmapTarget
import github.o4x.musical.imageloader.glide.targets.palette.PaletteTargetListener
import github.o4x.musical.interfaces.MusicServiceEventListener
import github.o4x.musical.prefs.HomeHeaderPref
import github.o4x.musical.prefs.PreferenceUtil
import github.o4x.musical.repository.SongRepository
import github.o4x.musical.util.CoverUtil
import github.o4x.musical.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class HomeHeaderViewModel(val songRepository: SongRepository) : ViewModel(),
    MusicServiceEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val posterBitmap = MutableLiveData<Bitmap>()
    fun getPosterBitmap(): LiveData<Bitmap> = posterBitmap

    init {
        fetchPosterBitmap()
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
        HomeHeaderPref.registerOnSharedPreferenceChangedListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
        HomeHeaderPref.unregisterOnSharedPreferenceChangedListener(this)
    }

    private fun fetchPosterBitmap() {
        val listener = object : PaletteTargetListener(App.getContext()) {
            override fun onColorReady(colors: MyPalette, resource: Bitmap?) {
                if (resource == null) return
                posterBitmap.postValue(resource)
            }
        }

        val loader = GlideLoader.with(App.getContext())
            .withListener(listener)

        var finisher: GlideLoader.GlideBuilder.GlideFinisher? = null

        viewModelScope.launch(Dispatchers.IO) {
            when (HomeHeaderPref.homeHeaderType) {
                HomeHeaderPref.TYPE_CUSTOM -> {
                    val uri = Uri.parse(HomeHeaderPref.customImagePath)
                    finisher = loader
                        .load(uri)
                }
                HomeHeaderPref.TYPE_SONG -> {
                    val song =
                        songRepository.song(HomeHeaderPref.imageSongID)
                    finisher = loader
                        .load(song)
                }
                HomeHeaderPref.TYPE_DEFAULT -> {
                    finisher = null
                }
            }

            withContext(Dispatchers.Main) {
                if (finisher == null) {
                    listener.onResourceReady(
                        CharCoverDrawable.empty()
                            .toBitmap(Util.getMaxScreenSize(), Util.getMaxScreenSize())
                    )
                } else {
                    finisher?.into(
                        CustomBitmapTarget(
                            Util.getMaxScreenSize(), Util.getMaxScreenSize()
                        )
                    )
                }
            }
        }
    }

    fun calculateBitmap(image: ImageView, it: Bitmap, w: Int, h: Int) {
        if (w <= 0 || h <= 0)
            return
        val m = max(w, h)
        viewModelScope.launch(Dispatchers.Default) {
            val paletteBuilder = Palette.from(it)
            val colors = MyPalette(
                image.context,
                paletteBuilder.generate()
            )
            var bitmap = Bitmap
                .createBitmap(
                    Bitmap.createScaledBitmap(it, m, m, false),
                    (m / 2) - (w / 2),
                    (m / 2) - (h / 2),
                    w,
                    h)
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

    override fun onMediaStoreChanged() {
        fetchPosterBitmap()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceUtil.GENERAL_THEME,
            HomeHeaderPref.CHANGE -> {
                fetchPosterBitmap()
            }
        }
    }

}