package github.o4x.musical.ui.fragments.player

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter
import androidx.lifecycle.lifecycleScope
import github.o4x.musical.R
import github.o4x.musical.databinding.FragmentPlayerPlaybackControlsBinding
import github.o4x.musical.drawables.PlayPauseDrawable
import github.o4x.musical.helper.MusicPlayerRemote
import github.o4x.musical.helper.PlayPauseButtonOnClickHandler
import github.o4x.musical.model.Song
import github.o4x.musical.service.MusicService
import github.o4x.musical.ui.dialogs.AddToPlaylistDialog
import github.o4x.musical.ui.fragments.AbsMusicServiceFragment
import github.o4x.musical.util.ColorUtil
import github.o4x.musical.util.MusicUtil
import github.o4x.musical.util.TintHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class PlayerPlaybackControlsFragments :
    AbsMusicServiceFragment(R.layout.fragment_player_playback_controls) {

    private var _binding: FragmentPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    private var playPauseDrawable: PlayPauseDrawable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerPlaybackControlsBinding.inflate(inflater, container, false)
        binding.playerViewModel = serviceActivity.playerViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setColor()
        binding.title.isSelected = true
        setUpMusicControllers()

        playerViewModel.position.observe(viewLifecycleOwner, {
            updateIsFavorite()
        })
        playerViewModel.isPlaying.observe(viewLifecycleOwner, {
            updatePlayPauseDrawableState()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        binding.playerAdd.setOnClickListener {
            AddToPlaylistDialog
                .create(MusicPlayerRemote.currentSong).show(childFragmentManager, "ADD_PLAYLIST")
        }
    }

    private fun setUpPlayPauseButton() {
        playPauseDrawable =
            PlayPauseDrawable(requireActivity())
        binding.playerPlayPauseButton.setImageDrawable(playPauseDrawable)
        binding.playerPlayPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updatePlayPauseDrawableState() {
        val animate = true
        if (MusicPlayerRemote.isPlaying) {
            playPauseDrawable?.setPause(animate)
        } else {
            playPauseDrawable?.setPlay(animate)
        }
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseButton()
        setUpProgressSlider()
        setupFavourite()
        setupMenu()
    }

    private fun setupFavourite() {
        binding.songFavourite.setOnClickListener {
            toggleFavorite(MusicPlayerRemote.currentSong)
        }
    }

    private fun setUpProgressSlider() {
        binding.playerProgressSlider.setOnSeekBarChangeListener(serviceActivity.playerViewModel)
    }


    private fun setColor() {
        val tintList = ColorStateList.valueOf(primaryColor)

        binding.playerAdd.imageTintList = tintList
        binding.songFavourite.imageTintList = tintList
        binding.playerProgressSlider.applyColor(primaryColor)
        binding.title.setTextColor(primaryColor)
        binding.text.setTextColor(secondaryColor)
        binding.playerSongCurrentProgress.setTextColor(secondaryColor)
        binding.playerSongTotalTime.setTextColor(secondaryColor)

        binding.playerPlayPauseButton.backgroundTintList = tintList
        binding.playerPlayPauseButton.imageTintList = ColorStateList.valueOf(backgroundColor)
        binding.playerPlayPauseButton.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)

        binding.playerNextButton.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)
        binding.playerPrevButton.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)

        binding.playerSongTotalTime.setTextColor(primaryColor)
        binding.playerSongCurrentProgress.setTextColor(primaryColor)

        binding.playerProgressSlider.applyColor(primaryColor)
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
}

private const val backgroundColor = Color.BLACK
private const val primaryColor = Color.WHITE
private val secondaryColor = ColorUtil.withAlpha(primaryColor, 0.6f)
private val disabledPrimaryColor = ColorUtil.withAlpha(primaryColor, 0.3f)

@BindingAdapter("repeatMode")
fun setRepeatMode(view: ImageView, mode: Int) {
    when (mode) {
        MusicService.REPEAT_MODE_ALL -> view.setImageResource(R.drawable.ic_repeat)
        MusicService.REPEAT_MODE_THIS -> view.setImageResource(R.drawable.ic_repeat_one)
        else -> view.setImageResource(R.drawable.ic_repeat)
    }

    view.setColorMode(mode != MusicService.REPEAT_MODE_NONE)
}

@BindingAdapter("shuffleMode")
fun setShuffleMode(view: ImageView, mode: Int) {
    when (mode) {
        MusicService.SHUFFLE_MODE_SHUFFLE -> view.setColorMode(true)
        else -> view.setColorMode(false)
    }
}

private fun ImageView.setColorMode(enable: Boolean) {
    setColorFilter(
        if (enable)
            primaryColor
        else
            disabledPrimaryColor,
        PorterDuff.Mode.SRC_IN
    )
}

fun SeekBar.applyColor(@ColorInt color: Int) {
    val tintList = ColorStateList.valueOf(color)

    thumbTintList = tintList
    progressTintList = tintList
    progressBackgroundTintList = tintList
}