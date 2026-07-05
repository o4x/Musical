package github.o4x.m2.ui.fragments.player

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import github.o4x.m2.R
import github.o4x.m2.databinding.FragmentPlayerBinding
import github.o4x.m2.helper.MusicPlayerRemote
import github.o4x.m2.helper.MusicPlayerRemote.currentSong
import github.o4x.m2.helper.MusicPlayerRemote.playingQueue
import github.o4x.m2.helper.menu.SongMenuHelper
import github.o4x.m2.model.lyrics.AbsSynchronizedLyrics
import github.o4x.m2.model.lyrics.Lyrics
import github.o4x.m2.prefs.PreferenceUtil
import github.o4x.m2.ui.activities.PlayerActivity
import github.o4x.m2.ui.adapter.cover.AlbumCoverPagerAdapter
import github.o4x.m2.ui.adapter.cover.BaseCoverPagerAdapter
import github.o4x.m2.ui.dialogs.CreatePlaylistDialog
import github.o4x.m2.ui.dialogs.LyricsDialog.Companion.create
import github.o4x.m2.ui.fragments.AbsMusicServiceFragment
import github.o4x.m2.util.MusicUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class PlayerFragment : AbsMusicServiceFragment(R.layout.fragment_player),
    ViewPager.OnPageChangeListener {

    private val playerActivity by lazy { activity as PlayerActivity }

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    val playbackControlsFragment by lazy {
        childFragmentManager.findFragmentById(R.id.playback_controls_fragment)
                as PlayerPlaybackControlsFragments
    }

    var lyrics: Lyrics? = null
    private var lyricsJob: kotlinx.coroutines.Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)

        playerActivity.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        playerActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        playerActivity.supportActionBar?.setDisplayShowHomeEnabled(true)
        playerActivity.supportActionBar?.title = null

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.frontAlbumArtPager.removeOnPageChangeListener(this)
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPagers()
        playerViewModel.currentSong.observe(viewLifecycleOwner, {
            updateLyrics()
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_player, menu)
        super.onCreateOptionsMenu(menu, inflater)
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
            if (binding.backAlbumArtPager.adapter == null) {
                binding.frontAlbumArtPager.adapter =
                    BaseCoverPagerAdapter(childFragmentManager, it)
                binding.frontAlbumArtPager.currentItem = MusicPlayerRemote.position

                binding.backAlbumArtPager.adapter =
                    AlbumCoverPagerAdapter(childFragmentManager, it)
                binding.backAlbumArtPager.currentItem = MusicPlayerRemote.position
            } else {
                (binding.backAlbumArtPager.adapter as AlbumCoverPagerAdapter).swapData(it)
                (binding.frontAlbumArtPager.adapter as BaseCoverPagerAdapter).swapData(it)
            }
        })
        playerViewModel.position.observe(viewLifecycleOwner, { newPosition ->
            if (binding.frontAlbumArtPager.currentItem != newPosition) {
                binding.frontAlbumArtPager.setCurrentItem(newPosition, true)
            }
            if (binding.backAlbumArtPager.currentItem != newPosition) {
                binding.backAlbumArtPager.setCurrentItem(newPosition, true)
            }
        })
        playerViewModel.progress.observe(viewLifecycleOwner, {
            updateLyricsProgress(it)
        })
    }

    private fun updateLyrics() {
        val song = currentSong
        lyricsJob?.cancel()
        lyricsJob = lifecycleScope.launch(Dispatchers.IO) {
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
                if (_binding != null) {
                    binding.toolbar.menu.findItem(R.id.action_lyrics)?.isVisible = lyrics != null
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val song = currentSong

        if (SongMenuHelper
                .handleMenuClick(requireActivity(),song, item.itemId))
                    return true

        when (item.itemId) {
            R.id.action_lyrics -> {
                if (lyrics != null)
                    create(lyrics!!).show(childFragmentManager, "LYRICS")

                return true
            }
            R.id.action_save_playing_queue -> {
                CreatePlaylistDialog.create(playingQueue)
                    .show(serviceActivity.supportFragmentManager, "ADD_TO_PLAYLIST")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun swapLyrics(l: Lyrics?) {
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

    private fun updateLyricsProgress(progress: Int) {
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

    // The page the user swiped to, applied once the pager stops moving.
    private var pendingPosition = -1

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        // Don't switch tracks here: onPageSelected fires mid-settle, and
        // MusicPlayerRemote.position re-prepares ExoPlayer and rebuilds the
        // notification synchronously on the main thread — doing that while the
        // pager is still animating drops frames. Record it and apply it on idle.
        pendingPosition = position
    }
    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            val target = pendingPosition
            pendingPosition = -1
            if (target >= 0 && target != MusicPlayerRemote.position) {
                MusicPlayerRemote.position = target
            }
        }
    }

    // The Next/Prev buttons animate the pager instead of switching the track
    // directly. The actual (main-thread-heavy) song change then happens once the
    // pager settles, via onPageScrollStateChanged — the same path a finger swipe
    // takes — so the animation runs without the re-prepare stalling its frames.
    fun nextPage() {
        val pager = _binding?.frontAlbumArtPager ?: return
        val count = pager.adapter?.count ?: return
        if (pager.currentItem < count - 1) {
            pager.setCurrentItem(pager.currentItem + 1, true)
        } else {
            // End of the queue: let the service apply repeat/stop semantics.
            MusicPlayerRemote.playNextSong()
        }
    }

    fun previousPage() {
        // Preserve "restart the current track when we're past the intro".
        if (MusicPlayerRemote.songProgressMillis > 5000) {
            MusicPlayerRemote.seekTo(0)
            return
        }
        val pager = _binding?.frontAlbumArtPager ?: return
        if (pager.currentItem > 0) {
            pager.setCurrentItem(pager.currentItem - 1, true)
        } else {
            // Start of the queue: let the service apply repeat/wrap semantics.
            MusicPlayerRemote.back()
        }
    }

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
                view.alpha = 1.0f - abs(position)
            }
            // Applying alpha < 1 to this full-screen page (a FrameLayout with a
            // background + ImageView, so hasOverlappingRendering() is true) makes the
            // renderer allocate and composite a full-screen off-screen buffer every
            // frame, which drops frames while two pages cross-fade during a scroll.
            // Promoting the page to a hardware layer for the duration of the fade
            // rasterizes it to a GPU texture once, so the per-frame alpha is cheap.
            // setLayerType() early-returns when the type is unchanged, so calling it
            // every frame is fine.
            val fading = view.alpha > 0f && view.alpha < 1f
            view.setLayerType(
                if (fading) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_NONE,
                null
            )
        }
    }

    class ParallaxPageTransformer : ViewPager.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            val pageWidth = view.width
            if (position in -1.0f..1.0f) {
                var playerImage = view.getTag(R.id.player_image) as? View
                if (playerImage == null) {
                    playerImage = view.findViewById(R.id.player_image)
                    view.setTag(R.id.player_image, playerImage)
                }
                playerImage?.translationX = -position * pageWidth / 2f
            }
        }
    }


    companion object {
        const val VISIBILITY_ANIM_DURATION = 300
        private const val primaryColor = Color.WHITE
    }
}
