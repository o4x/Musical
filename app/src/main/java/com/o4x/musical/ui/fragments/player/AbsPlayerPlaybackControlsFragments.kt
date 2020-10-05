package com.o4x.musical.ui.fragments.player

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.TintHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.MusicProgressViewUpdateHelper
import com.o4x.musical.helper.PlayPauseButtonOnClickHandler
import com.o4x.musical.model.Song
import com.o4x.musical.service.MusicService
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.color.MediaNotificationProcessor
import com.o4x.musical.views.PlayPauseDrawable
import java.util.*

abstract class AbsPlayerPlaybackControlsFragments : AbsMusicServiceFragment(),
    MusicProgressViewUpdateHelper.Callback, PopupMenu.OnMenuItemClickListener {
    private var unbinder: Unbinder? = null

    @JvmField
    @BindView(R.id.title)
    var title: MaterialTextView? = null

    @JvmField
    @BindView(R.id.text)
    var text: MaterialTextView? = null

    @JvmField
    @BindView(R.id.playerMenu)
    var playerMenu: AppCompatImageView? = null

    @JvmField
    @BindView(R.id.songFavourite)
    var songFavourite: AppCompatImageView? = null

    @JvmField
    @BindView(R.id.player_play_pause_button)
    var playPauseButton: FloatingActionButton? = null

    @JvmField
    @BindView(R.id.player_prev_button)
    var prevButton: ImageButton? = null

    @JvmField
    @BindView(R.id.player_next_button)
    var nextButton: ImageButton? = null

    @JvmField
    @BindView(R.id.player_repeat_button)
    var repeatButton: ImageButton? = null

    @JvmField
    @BindView(R.id.player_shuffle_button)
    var shuffleButton: ImageButton? = null

    @JvmField
    @BindView(R.id.player_progress_slider)
    var progressSlider: Slider? = null

    @JvmField
    @BindView(R.id.player_song_total_time)
    var songTotalTime: TextView? = null

    @JvmField
    @BindView(R.id.player_song_current_progress)
    var songCurrentProgress: TextView? = null

    private var playPauseDrawable: PlayPauseDrawable? = null
    private var lastPlaybackControlsColor = 0
    private var lastDisabledPlaybackControlsColor = 0
    private var progressViewUpdateHelper: MusicProgressViewUpdateHelper? = null
    private var musicControllerAnimationSet: AnimatorSet? = null
    private var hidden = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutRes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbinder = ButterKnife.bind(this, view)

        setupTitle()
        setUpMusicControllers()
        updateProgressTextColor()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder!!.unbind()
    }

    override fun onResume() {
        super.onResume()
        onUpdateProgressViews(MusicPlayerRemote.songProgressMillis,
            MusicPlayerRemote.songDurationMillis)
        progressViewUpdateHelper!!.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper!!.stop()
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
        title?.text = song.title
        text?.text = song.artistName
        updateIsFavorite()
    }

    private fun setupTitle() {
        title?.isSelected = true
    }

    private fun setupMenu() {
        playerMenu?.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.inflate(R.menu.menu_player)
            popupMenu.show()
        }
    }

    private fun setUpPlayPauseButton() {
        playPauseDrawable = PlayPauseDrawable(requireActivity())
        playPauseButton?.setImageDrawable(playPauseDrawable)
        updatePlayPauseColor()
        playPauseButton!!.setOnClickListener(PlayPauseButtonOnClickHandler())
        playPauseButton!!.post {
            if (playPauseButton != null) {
                playPauseButton!!.pivotX = playPauseButton!!.width / 2.toFloat()
                playPauseButton!!.pivotY = playPauseButton!!.height / 2.toFloat()
            }
        }
    }

    protected fun updatePlayPauseDrawableState(animate: Boolean) {
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
        nextButton!!.setOnClickListener { v: View? -> MusicPlayerRemote.playNextSong() }
        prevButton!!.setOnClickListener { v: View? -> MusicPlayerRemote.back() }
    }

    private fun updateProgressTextColor() {
        songTotalTime!!.setTextColor(lastPlaybackControlsColor)
        songCurrentProgress!!.setTextColor(lastPlaybackControlsColor)
    }

    private fun updatePrevNextColor() {
        nextButton!!.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        prevButton!!.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    private fun updatePlayPauseColor() {
        playPauseButton!!.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setUpShuffleButton() {
        shuffleButton!!.setOnClickListener { v: View? -> MusicPlayerRemote.toggleShuffleMode() }
    }

    fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE -> shuffleButton?.setColorFilter(
                lastPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
            else -> shuffleButton?.setColorFilter(
                lastDisabledPlaybackControlsColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun setUpRepeatButton() {
        repeatButton!!.setOnClickListener { v: View? -> MusicPlayerRemote.cycleRepeatMode() }
    }

    private fun setupFavourite() {
        songFavourite?.setOnClickListener {
            toggleFavorite(MusicPlayerRemote.currentSong)
        }
    }

    private fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                repeatButton!!.setImageResource(R.drawable.ic_repeat_white_24dp)
                repeatButton!!.setColorFilter(lastDisabledPlaybackControlsColor,
                    PorterDuff.Mode.SRC_IN)
            }
            MusicService.REPEAT_MODE_ALL -> {
                repeatButton!!.setImageResource(R.drawable.ic_repeat_white_24dp)
                repeatButton!!.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
            MusicService.REPEAT_MODE_THIS -> {
                repeatButton!!.setImageResource(R.drawable.ic_repeat_one_white_24dp)
                repeatButton!!.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    fun show() {
        if (hidden) {
            if (musicControllerAnimationSet == null) {
                val interpolator: TimeInterpolator = FastOutSlowInInterpolator()
                val duration = 300
                val animators = LinkedList<Animator>()
                addAnimation(animators, playPauseButton, interpolator, duration, 0)
                addAnimation(animators, nextButton, interpolator, duration, 100)
                addAnimation(animators, prevButton, interpolator, duration, 100)
                addAnimation(animators, shuffleButton, interpolator, duration, 200)
                addAnimation(animators, repeatButton, interpolator, duration, 200)
                musicControllerAnimationSet = AnimatorSet()
                musicControllerAnimationSet!!.playTogether(animators)
            } else {
                musicControllerAnimationSet!!.cancel()
            }
            musicControllerAnimationSet!!.start()
        }
        hidden = false
    }

    fun hide() {
        if (musicControllerAnimationSet != null) {
            musicControllerAnimationSet!!.cancel()
        }
        prepareForAnimation(playPauseButton)
        prepareForAnimation(nextButton)
        prepareForAnimation(prevButton)
        prepareForAnimation(shuffleButton)
        prepareForAnimation(repeatButton)
        hidden = true
    }

    private fun setUpProgressSlider() {
        updateProgressSliderColor()

        progressSlider?.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                MusicPlayerRemote.seekTo(slider.value.toInt())
                onUpdateProgressViews(MusicPlayerRemote.songProgressMillis,
                    MusicPlayerRemote.songDurationMillis)
            }

        })

        progressSlider?.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {

            }
        }
    }

    private fun updateProgressSliderColor() {
        progressSlider?.applyColor(lastPlaybackControlsColor)
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        if (total > progress) {
            progressSlider?.valueTo = total.toFloat()
            progressSlider?.value = progress.toFloat()
        }
        songTotalTime?.text = MusicUtil.getReadableDurationString(total.toLong())
        songCurrentProgress!!.text = MusicUtil.getReadableDurationString(progress.toLong())
    }

    @get:LayoutRes
    protected abstract val layoutRes: Int


    fun setColor(colors: MediaNotificationProcessor) {
        lastPlaybackControlsColor = colors.primaryTextColor
        lastDisabledPlaybackControlsColor = ColorUtil.withAlpha(colors.primaryTextColor, 0.3f)

        val tintList = ColorStateList.valueOf(colors.primaryTextColor)
        playerMenu?.imageTintList = tintList
        songFavourite?.imageTintList = tintList
        progressSlider?.applyColor(colors.primaryTextColor)
        title?.setTextColor(colors.primaryTextColor)
        text?.setTextColor(colors.secondaryTextColor)
        songCurrentProgress?.setTextColor(colors.secondaryTextColor)
        songTotalTime?.setTextColor(colors.secondaryTextColor)

        playPauseButton?.backgroundTintList = tintList
        playPauseButton?.imageTintList = ColorStateList.valueOf(colors.backgroundColor)
        playPauseButton!!.setColorFilter(colors.backgroundColor, PorterDuff.Mode.SRC_IN)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
//        updatePlayPauseColor()
        updateProgressTextColor()
        updateProgressSliderColor()
    }

    private fun toggleFavorite(song: Song) {
        MusicUtil.toggleFavorite(requireContext(), song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }


    private var updateIsFavoriteTask: AsyncTask<*, *, *>? = null

    @SuppressLint("StaticFieldLeak")
    fun updateIsFavorite() {
        if (updateIsFavoriteTask != null) {
            updateIsFavoriteTask?.cancel(false)
        }
        updateIsFavoriteTask = object : AsyncTask<Song, Void, Boolean>() {
            override fun doInBackground(vararg params: Song): Boolean? {
                val activity = activity
                return if (activity != null) {
                    MusicUtil.isFavorite(requireActivity(), params[0])
                } else {
                    cancel(false)
                    null
                }
            }

            override fun onPostExecute(isFavorite: Boolean?) {
                val activity = activity
                if (activity != null) {
                    val res = if (isFavorite!!)
                        R.drawable.ic_favorite_white_24dp
                    else
                        R.drawable.ic_favorite_border_white_24dp

                    val drawable = TintHelper.createTintedDrawable(activity, res, Color.WHITE)
                    songFavourite?.setImageDrawable(drawable)
                }
            }
        }.execute(MusicPlayerRemote.currentSong)
    }

    fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    companion object {
        private fun addAnimation(
            animators: MutableCollection<Animator>,
            view: View?,
            interpolator: TimeInterpolator,
            duration: Int,
            delay: Int
        ) {
            val scaleX: Animator = ObjectAnimator.ofFloat(view, View.SCALE_X, 0f, 1f)
            scaleX.interpolator = interpolator
            scaleX.duration = duration.toLong()
            scaleX.startDelay = delay.toLong()
            animators.add(scaleX)
            val scaleY: Animator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0f, 1f)
            scaleY.interpolator = interpolator
            scaleY.duration = duration.toLong()
            scaleY.startDelay = delay.toLong()
            animators.add(scaleY)
        }

        private fun prepareForAnimation(view: View?) {
            if (view != null) {
                view.scaleX = 0f
                view.scaleY = 0f
            }
        }
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