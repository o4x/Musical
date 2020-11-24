package com.o4x.musical.ui.fragments.player

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentPlayerBinding
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.MusicPlayerRemote.clearQueue
import com.o4x.musical.helper.MusicPlayerRemote.currentSong
import com.o4x.musical.helper.MusicPlayerRemote.playingQueue
import com.o4x.musical.model.lyrics.AbsSynchronizedLyrics
import com.o4x.musical.model.lyrics.Lyrics
import com.o4x.musical.ui.activities.tageditor.AbsTagEditorActivity
import com.o4x.musical.ui.activities.tageditor.SongTagEditorActivity
import com.o4x.musical.ui.adapter.cover.AlbumCoverPagerAdapter
import com.o4x.musical.ui.adapter.cover.BaseCoverPagerAdapter
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog.Companion.create
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.dialogs.LyricsDialog.Companion.create
import com.o4x.musical.ui.dialogs.SleepTimerDialog
import com.o4x.musical.ui.dialogs.SongDetailDialog
import com.o4x.musical.ui.dialogs.SongShareDialog
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerFragment : AbsMusicServiceFragment(R.layout.fragment_player),
    Toolbar.OnMenuItemClickListener, ViewPager.OnPageChangeListener {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    val playbackControlsFragment by lazy {
        childFragmentManager.findFragmentById(R.id.playback_controls_fragment)
                as PlayerPlaybackControlsFragments
    }

    var lyrics: Lyrics? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.frontAlbumArtPager.removeOnPageChangeListener(this)
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpPlayerToolbar()
        setupPagers()

        playerViewModel.currentSong.observe(viewLifecycleOwner, {
            updateLyrics()
        })
    }

    private fun setUpPlayerToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_player)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { serviceActivity.onBackPressed() }
        binding.toolbar.setOnMenuItemClickListener(this)
    }

    private fun setupPagers() {
        binding.backAlbumArtPager.addSyncViewPager(binding.frontAlbumArtPager)
        binding.frontAlbumArtPager.addSyncViewPager(binding.backAlbumArtPager)

        binding.frontAlbumArtPager
            .addOnPageChangeListener(this)
        binding.backAlbumArtPager
            .setPageTransformer(true, FadePageTransformer())
        binding.frontAlbumArtPager
            .setPageTransformer(true, ParallaxPageTransformer())

        playerViewModel.queue.observe(viewLifecycleOwner, {
            binding.frontAlbumArtPager.adapter =
                BaseCoverPagerAdapter(childFragmentManager, it)
            binding.frontAlbumArtPager.currentItem = MusicPlayerRemote.position

            binding.backAlbumArtPager.adapter =
                AlbumCoverPagerAdapter(childFragmentManager, it)
            binding.backAlbumArtPager.currentItem = MusicPlayerRemote.position

        })
        playerViewModel.position.observe(viewLifecycleOwner, {
            binding.frontAlbumArtPager.setCurrentItem(it, true)
            binding.backAlbumArtPager.setCurrentItem(it, true)
        })
        playerViewModel.progress.observe(viewLifecycleOwner, {
            updateLyricsProgress(it)
        })
    }

    private fun updateLyrics() {
        val song = currentSong
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                lyrics = null
                swapLyrics(null)
            }

            val data = MusicUtil.getLyrics(song)
            lyrics = if (TextUtils.isEmpty(data)) {
                null
            } else Lyrics.parse(song, data)

            withContext(Dispatchers.Main) {
                swapLyrics(lyrics)

                binding.toolbar.menu.findItem(R.id.action_lyrics)
                    .isVisible = lyrics != null
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val song = currentSong
        when (item.itemId) {
            R.id.action_lyrics -> {
                if (lyrics != null)
                    create(lyrics!!).show(childFragmentManager, "LYRICS")

                return true
            }
            R.id.action_sleep_timer -> {
                SleepTimerDialog().show(childFragmentManager, "SET_SLEEP_TIMER")
                return true
            }
            R.id.action_share -> {
                SongShareDialog.create(song).show(childFragmentManager, "SHARE_SONG")
                return true
            }
            R.id.action_equalizer -> {
                NavigationUtil.openEqualizer(serviceActivity)
                return true
            }
            R.id.action_add_to_playlist -> {
                create(song).show(childFragmentManager, "ADD_PLAYLIST")
                return true
            }
            R.id.action_clear_playing_queue -> {
                clearQueue()
                return true
            }
            R.id.action_save_playing_queue -> {
                CreatePlaylistDialog.create(playingQueue)
                    .show(serviceActivity.supportFragmentManager, "ADD_TO_PLAYLIST")
                return true
            }
            R.id.action_tag_editor -> {
                val intent = Intent(serviceActivity, SongTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id)
                startActivity(intent)
                return true
            }
            R.id.action_details -> {
                SongDetailDialog.create(song).show(childFragmentManager, "SONG_DETAIL")
                return true
            }
            R.id.action_go_to_album -> {
                NavigationUtil.goToAlbum(serviceActivity, song.albumId)
                return true
            }
            R.id.action_go_to_artist -> {
                NavigationUtil.goToArtist(serviceActivity, song.artistId)
                return true
            }
        }
        return false
    }

    private val isLyricsLayoutVisible: Boolean
        get() = lyrics != null && lyrics!!.isSynchronized && lyrics!!.isValid && PreferenceUtil.synchronizedLyricsShow()

    private fun hideLyricsLayout() {
        binding.playerLyrics.animate().alpha(0f).setDuration(VISIBILITY_ANIM_DURATION.toLong())
            .withEndAction {
                if (_binding == null) return@withEndAction
                binding.playerLyrics.visibility = View.GONE
                binding.playerLyricsLine1.text = null
                binding.playerLyricsLine2.text = null
            }
    }

    fun swapLyrics(l: Lyrics?) {
        lyrics = l
        if (!isLyricsLayoutVisible) {
            hideLyricsLayout()
            return
        }
        binding.playerLyricsLine1.text = null
        binding.playerLyricsLine2.text = null
        binding.playerLyrics.visibility = View.VISIBLE
        binding.playerLyrics.animate().alpha(1f).duration = VISIBILITY_ANIM_DURATION.toLong()
    }

    fun updateLyricsProgress(progress: Int) {
        if (!isLyricsLayoutVisible) {
            hideLyricsLayout()
            return
        }
        if (lyrics !is AbsSynchronizedLyrics) return
        val synchronizedLyrics = lyrics as AbsSynchronizedLyrics
        binding.playerLyrics.visibility = View.VISIBLE
        binding.playerLyrics.alpha = 1f
        val oldLine = binding.playerLyricsLine2.text.toString()
        val line = synchronizedLyrics.getLine(progress)
        if (oldLine != line || oldLine.isEmpty()) {
            binding.playerLyricsLine1.text = oldLine
            binding.playerLyricsLine2.text = line
            binding.playerLyricsLine1.visibility = View.VISIBLE
            binding.playerLyricsLine2.visibility = View.VISIBLE
            binding.playerLyricsLine2.measure(
                View.MeasureSpec.makeMeasureSpec(
                    binding.playerLyricsLine2.measuredWidth,
                    View.MeasureSpec.EXACTLY
                ), View.MeasureSpec.UNSPECIFIED
            )
            val h = binding.playerLyricsLine2.measuredHeight
            binding.playerLyricsLine1.alpha = 1f
            binding.playerLyricsLine1.translationY = 0f
            binding.playerLyricsLine1.animate().alpha(0f).translationY(-h.toFloat()).duration =
                VISIBILITY_ANIM_DURATION.toLong()
            binding.playerLyricsLine2.alpha = 0f
            binding.playerLyricsLine2.translationY = h.toFloat()
            binding.playerLyricsLine2.animate().alpha(1f).translationY(0f).duration =
                VISIBILITY_ANIM_DURATION.toLong()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        if (position != MusicPlayerRemote.position) {
            MusicPlayerRemote.position = position
        }
    }
    override fun onPageScrollStateChanged(state: Int) {}

    class FadePageTransformer : ViewPager.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            if (position <= -1.0f || position >= 1.0f) {
                view.translationX = view.width * position
                view.alpha = 0.0f
            } else if (position == 0.0f) {
                view.translationX = view.width * position
                view.alpha = 1.0f
            } else {
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                view.translationX = view.width * -position
                view.alpha = 1.0f - Math.abs(position)
            }
        }
    }

    class ParallaxPageTransformer : ViewPager.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            val pageWidth = view.width
            val pageHeight = view.height

            if (position <= 1) { // [-1,1]
                view.findViewById<View>(R.id.player_image)
                    .translationX = -position * pageWidth / 2
            }
        }
    }

    companion object {
        const val VISIBILITY_ANIM_DURATION = 300
    }
}