package com.o4x.musical.ui.fragments.player

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.TintHelper
import com.google.android.material.slider.Slider
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentPlayerPlaybackControlsBinding
import com.o4x.musical.drawables.PlayPauseDrawable
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.PlayPauseButtonOnClickHandler
import com.o4x.musical.model.Song
import com.o4x.musical.service.MusicService
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.color.MediaNotificationProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

open class PlayerPlaybackControlsFragments :
    AbsMusicServiceFragment(R.layout.fragment_player_playback_controls), PopupMenu.OnMenuItemClickListener {

    private var _binding: FragmentPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    private var playPauseDrawable: PlayPauseDrawable? = null
    private var lastPlaybackControlsColor = 0
    private var lastDisabledPlaybackControlsColor = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerPlaybackControlsBinding.inflate(inflater, container, false)
        binding.progressViewModel = serviceActivity.playerViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTitle()
        setUpMusicControllers()
        updateProgressTextColor()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return (parentFragment as AbsPlayerFragment).onMenuItemClick(item!!)
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState(true)
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState(true)
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.text.text = song.artistName
        updateIsFavorite()
    }

    private fun setupTitle() {
        binding.title.isSelected = true
    }

    private fun setupMenu() {
        binding.playerMenu.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.inflate(R.menu.menu_player)
            popupMenu.show()
        }
    }

    private fun setUpPlayPauseButton() {
        playPauseDrawable =
            PlayPauseDrawable(requireActivity())
        binding.playerPlayPauseButton.setImageDrawable(playPauseDrawable)
        updatePlayPauseColor()
        binding.playerPlayPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
        binding.playerPlayPauseButton.post {
            binding.playerPlayPauseButton.pivotX = binding.playerPlayPauseButton.width / 2.toFloat()
            binding.playerPlayPauseButton.pivotY = binding.playerPlayPauseButton.height / 2.toFloat()
        }
    }

    private fun updatePlayPauseDrawableState(animate: Boolean) {
        if (MusicPlayerRemote.isPlaying) {
            playPauseDrawable!!.setPause(animate)
        } else {
            playPauseDrawable!!.setPlay(animate)
        }
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseButton()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
        setupFavourite()
        setupMenu()
    }

    private fun setUpPrevNext() {
        updatePrevNextColor()
        binding.playerNextButton.setOnClickListener { v: View? -> MusicPlayerRemote.playNextSong() }
        binding.playerPrevButton.setOnClickListener { v: View? -> MusicPlayerRemote.back() }
    }

    private fun updateProgressTextColor() {
        binding.playerSongTotalTime.setTextColor(lastPlaybackControlsColor)
        binding.playerSongCurrentProgress.setTextColor(lastPlaybackControlsColor)
    }

    private fun updatePrevNextColor() {
        binding.playerNextButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        binding.playerPrevButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    private fun updatePlayPauseColor() {
        binding.playerPlayPauseButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setUpShuffleButton() {
        binding.playerShuffleButton.setOnClickListener { v: View? -> MusicPlayerRemote.toggleShuffleMode() }
    }

    private fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE -> binding.playerShuffleButton.setColorFilter(
                lastPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
            else -> binding.playerShuffleButton.setColorFilter(
                lastDisabledPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setUpRepeatButton() {
        binding.playerRepeatButton.setOnClickListener { v: View? -> MusicPlayerRemote.cycleRepeatMode() }
    }

    private fun setupFavourite() {
        binding.songFavourite.setOnClickListener {
            toggleFavorite(MusicPlayerRemote.currentSong)
        }
    }

    private fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                binding.playerRepeatButton.setImageResource(R.drawable.ic_repeat)
                binding.playerRepeatButton.setColorFilter(lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN)
            }
            MusicService.REPEAT_MODE_ALL -> {
                binding.playerRepeatButton.setImageResource(R.drawable.ic_repeat)
                binding.playerRepeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
            MusicService.REPEAT_MODE_THIS -> {
                binding.playerRepeatButton.setImageResource(R.drawable.ic_repeat_one)
                binding.playerRepeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun setUpProgressSlider() {
        updateProgressSliderColor()

        binding.playerProgressSlider.setOnSeekBarChangeListener(serviceActivity.playerViewModel)
    }

    private fun updateProgressSliderColor() {
        binding.playerProgressSlider.applyColor(lastPlaybackControlsColor)
    }


    fun setColor(colors: MediaNotificationProcessor) {
        lastPlaybackControlsColor = colors.primaryTextColor
        lastDisabledPlaybackControlsColor = ColorUtil.withAlpha(colors.primaryTextColor, 0.3f)

        val tintList = ColorStateList.valueOf(colors.primaryTextColor)
        binding.playerMenu.imageTintList = tintList
        binding.songFavourite.imageTintList = tintList
        binding.playerProgressSlider.applyColor(colors.primaryTextColor)
        binding.title.setTextColor(colors.primaryTextColor)
        binding.text.setTextColor(colors.secondaryTextColor)
        binding.playerSongCurrentProgress.setTextColor(colors.secondaryTextColor)
        binding.playerSongTotalTime.setTextColor(colors.secondaryTextColor)

        binding.playerPlayPauseButton.backgroundTintList = tintList
        binding.playerPlayPauseButton.imageTintList = ColorStateList.valueOf(colors.backgroundColor)
        binding.playerPlayPauseButton.setColorFilter(colors.backgroundColor, PorterDuff.Mode.SRC_IN)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
//        updatePlayPauseColor()
        updateProgressTextColor()
        updateProgressSliderColor()
    }

    private fun toggleFavorite(song: Song) {
        lifecycleScope.launch(Dispatchers.IO) {
            MusicUtil.toggleFavorite(requireContext(), song)

            if (song.id == MusicPlayerRemote.currentSong.id) {
                withContext(Dispatchers.Main) {
                    updateIsFavorite()
                }
            }
        }
    }

    private fun updateIsFavorite() {

        lifecycleScope.launch(Dispatchers.IO) {
            val isFavorite =
                MusicUtil.isFavorite(requireContext(), MusicPlayerRemote.currentSong)

            withContext(Dispatchers.Main) {
                val res = if (isFavorite)
                    R.drawable.ic_star
                else
                    R.drawable.ic_star_border

                val drawable = TintHelper.createTintedDrawable(requireContext(), res, Color.WHITE)
                binding.songFavourite.setImageDrawable(drawable)
            }
        }
    }

    fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }
}


fun Slider.applyColor(@ColorInt color: Int) {

    val tintList = ColorStateList.valueOf(color)

    thumbTintList = tintList
    haloTintList = ColorStateList.valueOf(color)
    trackTintList = tintList
    trackActiveTintList = ColorStateList.valueOf(ColorUtil.withAlpha(color, 0.8f))
    trackInactiveTintList = ColorStateList.valueOf(ColorUtil.withAlpha(color, 0.3f))
}

fun SeekBar.applyColor(@ColorInt color: Int) {

    val tintList = ColorStateList.valueOf(color)

    thumbTintList = tintList
    indeterminateTintList = tintList
    progressBackgroundTintList = tintList
    secondaryProgressTintList = tintList
    tickMarkTintList = tintList
}