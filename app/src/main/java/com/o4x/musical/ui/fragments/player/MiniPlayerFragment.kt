package com.o4x.musical.ui.fragments.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import androidx.annotation.ColorInt
import code.name.monkey.appthemehelper.extensions.accentColor
import code.name.monkey.appthemehelper.extensions.textColorSecondary
import code.name.monkey.appthemehelper.util.ATHUtil.resolveColor
import code.name.monkey.appthemehelper.util.ColorUtil.withAlpha
import com.o4x.musical.R
import com.o4x.musical.ads.AdsUtils
import com.o4x.musical.databinding.FragmentMiniPlayerBinding
import com.o4x.musical.drawables.PlayPauseDrawable
import com.o4x.musical.helper.MusicPlayerRemote.currentSong
import com.o4x.musical.helper.MusicPlayerRemote.isPlaying
import com.o4x.musical.helper.MusicPlayerRemote.playNextSong
import com.o4x.musical.helper.MusicPlayerRemote.playPreviousSong
import com.o4x.musical.helper.PlayPauseButtonOnClickHandler
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment
import com.o4x.musical.util.color.MediaNotificationProcessor
import kotlin.math.abs

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
open class MiniPlayerFragment : AbsMusicServiceFragment(R.layout.fragment_mini_player) {

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding get() = _binding!!

    private var miniPlayerPlayPauseDrawable: PlayPauseDrawable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMiniPlayerBinding.inflate(inflater, container, false)
        binding.progressViewModel = serviceActivity.playerViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener(FlingPlayBackController(serviceActivity))
        setUpMiniPlayer()

        AdsUtils(requireActivity()).loadStandardBanner(binding.banner)
    }

    private fun setUpMiniPlayer() {
        setUpPlayPauseButton()
        setProgressColor(this.accentColor())
    }

    private fun setProgressColor(@ColorInt color: Int) {
        binding.progressBarContainer.setBackgroundColor(withAlpha(color, .3f))
        binding.progressBar.supportProgressTintList = ColorStateList.valueOf(color)
    }

    private fun setUpPlayPauseButton() {
        miniPlayerPlayPauseDrawable = PlayPauseDrawable(requireActivity())
        binding.miniPlayerPlayPauseButton.setImageDrawable(miniPlayerPlayPauseDrawable)
        binding.miniPlayerPlayPauseButton.setColorFilter(
            resolveColor(
                requireActivity(),
                R.attr.iconColor,
                requireActivity().textColorSecondary()
            ), PorterDuff.Mode.SRC_IN
        )
        binding.miniPlayerPlayPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updateSongTitle() {
        binding.miniPlayerTitle.text = currentSong.title
    }

    override fun onServiceConnected() {
        updateSongTitle()
        updatePlayPauseDrawableState(false)
    }

    override fun onPlayingMetaChanged() {
        updateSongTitle()
    }

    override fun onMediaStoreChanged() {
        updateSongTitle()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState(true)
    }

    private class FlingPlayBackController(context: Context?) : View.OnTouchListener {

        var flingPlayBackController: GestureDetector

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return flingPlayBackController.onTouchEvent(event)
        }

        init {
            flingPlayBackController =
                GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onFling(
                        e1: MotionEvent,
                        e2: MotionEvent,
                        velocityX: Float,
                        velocityY: Float
                    ): Boolean {
                        if (abs(velocityX) > abs(velocityY)) {
                            if (velocityX < 0) {
                                playNextSong()
                                return true
                            } else if (velocityX > 0) {
                                playPreviousSong()
                                return true
                            }
                        }
                        return false
                    }
                })
        }
    }

    private fun updatePlayPauseDrawableState(animate: Boolean) {
        if (isPlaying) {
            miniPlayerPlayPauseDrawable!!.setPause(animate)
        } else {
            miniPlayerPlayPauseDrawable!!.setPlay(animate)
        }
    }

    fun setColor(colors: MediaNotificationProcessor) {
        val fg = colors.primaryTextColor
        binding.container.setBackgroundColor(colors.backgroundColor)
        setProgressColor(fg)
        binding.miniPlayerImage.setColorFilter(fg)
        binding.miniPlayerPlayPauseButton.setColorFilter(fg, PorterDuff.Mode.SRC_IN)
        binding.miniPlayerTitle.setTextColor(fg)

        // I want title slide show just when it colored
        binding.miniPlayerTitle.isSelected = true
    }
}