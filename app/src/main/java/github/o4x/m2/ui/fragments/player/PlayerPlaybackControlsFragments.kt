package github.o4x.m2.ui.fragments.player

import android.R.attr.textColorPrimary
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.lifecycle.lifecycleScope
import github.o4x.m2.R
import github.o4x.m2.databinding.FragmentPlayerPlaybackControlsBinding
import github.o4x.m2.drawables.PlayPauseDrawable
import github.o4x.m2.helper.MusicPlayerRemote
import github.o4x.m2.helper.PlayPauseButtonOnClickHandler
import github.o4x.m2.model.Song
import github.o4x.m2.service.MusicService
import github.o4x.m2.ui.dialogs.AddToPlaylistDialog
import github.o4x.m2.ui.fragments.AbsMusicServiceFragment
import github.o4x.m2.util.ColorUtil
import github.o4x.m2.util.MusicUtil
import github.o4x.m2.util.TintHelper
import github.o4x.m2.util.backgroundColor
import github.o4x.m2.util.textColorPrimary
import github.o4x.m2.volume.AudioVolumeObserver
import github.o4x.m2.volume.OnAudioVolumeChangedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class PlayerPlaybackControlsFragments :
    AbsMusicServiceFragment(R.layout.fragment_player_playback_controls),
    OnAudioVolumeChangedListener {

    private var _binding: FragmentPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    private var playPauseDrawable: PlayPauseDrawable? = null

    private var audioVolumeObserver: AudioVolumeObserver? = null

    private val audioManager: AudioManager?
        get() = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerPlaybackControlsBinding.inflate(inflater, container, false)
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
        playerViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            binding.title.text = song.title
            binding.text.text = song.artistName
        }
        playerViewModel.progress.observe(viewLifecycleOwner) {
            binding.playerProgressSlider.progress = it
        }
        playerViewModel.total.observe(viewLifecycleOwner) {
            binding.playerProgressSlider.max = it
        }
        playerViewModel.progressText.observe(viewLifecycleOwner) {
            binding.playerSongCurrentProgress.text = it
        }
        playerViewModel.totalText.observe(viewLifecycleOwner) {
            binding.playerSongTotalTime.text = it
        }
        playerViewModel.repeatMode.observe(viewLifecycleOwner) {
            setRepeatMode(binding.playerRepeatButton, it)
        }
        playerViewModel.shuffleMode.observe(viewLifecycleOwner) {
            setShuffleMode(binding.playerShuffleButton, it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioVolumeObserver?.unregister()
        audioVolumeObserver = null
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
            PlayPauseDrawable(requireActivity(), Color.WHITE)
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
        setUpVolume()
        setupFavourite()
        setupMenu()

        binding.playerRepeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
        binding.playerShuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
        // Route through the pager so the cover animation plays first and the
        // track switch happens on settle (see PlayerFragment.nextPage/previousPage),
        // avoiding the mid-animation re-prepare stall. Fall back to a direct call
        // if this isn't hosted inside PlayerFragment (e.g. reused elsewhere).
        binding.playerPrevButton.setOnClickListener {
            (parentFragment as? PlayerFragment)?.previousPage() ?: MusicPlayerRemote.back()
        }
        binding.playerNextButton.setOnClickListener {
            (parentFragment as? PlayerFragment)?.nextPage() ?: MusicPlayerRemote.playNextSong()
        }
    }

    private fun setupFavourite() {
        binding.songFavourite.setOnClickListener {
            toggleFavorite(MusicPlayerRemote.currentSong)
        }
    }

    private fun setUpProgressSlider() {
        binding.playerProgressSlider.setOnSeekBarChangeListener(serviceActivity.playerViewModel)
    }

    private fun setUpVolume() {
        val am = audioManager ?: return
        binding.volumeSeekBar.max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.volumeSeekBar.progress = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        updateVolumeIcon()

        binding.volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                    updateVolumeIcon()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.volumeDown.setOnClickListener { setVolume(binding.volumeSeekBar.progress - 1) }
        binding.volumeUp.setOnClickListener { setVolume(binding.volumeSeekBar.progress + 1) }

        audioVolumeObserver = AudioVolumeObserver(requireContext()).apply {
            register(AudioManager.STREAM_MUSIC, this@PlayerPlaybackControlsFragments)
        }
    }

    private fun setVolume(volume: Int) {
        val am = audioManager ?: return
        val clamped = volume.coerceIn(0, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        am.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
        binding.volumeSeekBar.progress = clamped
        updateVolumeIcon()
    }

    private fun updateVolumeIcon() {
        if (_binding == null) return
        val res = if (binding.volumeSeekBar.progress == 0)
            R.drawable.ic_volume_off
        else
            R.drawable.ic_volume_down
        binding.volumeDown.setImageResource(res)
    }

    override fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int) {
        if (_binding == null) return
        binding.volumeSeekBar.max = maxVolume
        binding.volumeSeekBar.progress = currentVolume
        updateVolumeIcon()
    }


    private fun setColor() {
        val backgroundColor = backgroundColor()
        val primaryColor = textColorPrimary()
        val secondaryColor = ColorUtil.withAlpha(primaryColor, 0.6f)
        val tintList = ColorStateList.valueOf(primaryColor)

        binding.playerAdd.imageTintList = tintList
        binding.songFavourite.imageTintList = tintList
        binding.volumeDown.imageTintList = tintList
        binding.volumeUp.imageTintList = tintList
        binding.volumeSeekBar.applyColor(primaryColor)
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


fun setRepeatMode(view: ImageView, mode: Int) {
    when (mode) {
        MusicService.REPEAT_MODE_ALL -> view.setImageResource(R.drawable.ic_repeat)
        MusicService.REPEAT_MODE_THIS -> view.setImageResource(R.drawable.ic_repeat_one)
        else -> view.setImageResource(R.drawable.ic_repeat)
    }

    view.setColorMode(mode != MusicService.REPEAT_MODE_NONE)
}

fun setShuffleMode(view: ImageView, mode: Int) {
    when (mode) {
        MusicService.SHUFFLE_MODE_SHUFFLE -> view.setColorMode(true)
        else -> view.setColorMode(false)
    }
}

private fun ImageView.setColorMode(enable: Boolean) {
    val primaryColor = context.textColorPrimary()
    val disabledPrimaryColor = ColorUtil.withAlpha(primaryColor, 0.3f)
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