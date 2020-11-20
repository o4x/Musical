package com.o4x.musical.ui.fragments.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import code.name.monkey.appthemehelper.extensions.accentColor
import code.name.monkey.appthemehelper.extensions.textColorSecondary
import code.name.monkey.appthemehelper.util.ATHUtil.resolveColor
import code.name.monkey.appthemehelper.util.ColorUtil.withAlpha
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentMiniPlayerBinding
import com.o4x.musical.drawables.PlayPauseDrawable
import com.o4x.musical.helper.MusicPlayerRemote.currentSong
import com.o4x.musical.helper.MusicPlayerRemote.isPlaying
import com.o4x.musical.helper.MusicPlayerRemote.playNextSong
import com.o4x.musical.helper.MusicPlayerRemote.playPreviousSong
import com.o4x.musical.helper.MusicPlayerRemote.songDurationMillis
import com.o4x.musical.helper.MusicPlayerRemote.songProgressMillis
import com.o4x.musical.helper.MusicProgressViewUpdateHelper
import com.o4x.musical.helper.PlayPauseButtonOnClickHandler
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment
import com.o4x.musical.util.color.MediaNotificationProcessor
import com.o4x.musical.views.IconImageView
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import kotlin.math.abs

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
open class MiniPlayerFragment : AbsMusicServiceFragment(R.layout.fragment_mini_player),
    MusicProgressViewUpdateHelper.Callback {

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding get() = _binding!!

    private var miniPlayerPlayPauseDrawable: PlayPauseDrawable? = null
    private var progressViewUpdateHelper: MusicProgressViewUpdateHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMiniPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener(FlingPlayBackController(activity))
        setUpMiniPlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        onUpdateProgressViews(
            songProgressMillis,
            songDurationMillis
        )
    }

    override fun onPlayingMetaChanged() {
        updateSongTitle()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState(true)
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.progressBar.max = total
        binding.progressBar.progress = progress
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper!!.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper!!.stop()
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