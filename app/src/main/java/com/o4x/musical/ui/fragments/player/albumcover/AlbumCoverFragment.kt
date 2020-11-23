package com.o4x.musical.ui.fragments.player.albumcover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentPlayerAlbumCoverBinding
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.MusicPlayerRemote.position
import com.o4x.musical.model.lyrics.AbsSynchronizedLyrics
import com.o4x.musical.model.lyrics.Lyrics
import com.o4x.musical.ui.adapter.cover.BaseCoverPagerAdapter
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment
import com.o4x.musical.util.PreferenceUtil.synchronizedLyricsShow


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class AlbumCoverFragment : AbsMusicServiceFragment(R.layout.fragment_player_album_cover),
    OnPageChangeListener {

    private var _binding: FragmentPlayerAlbumCoverBinding? = null
    private val binding get() = _binding!!

    private var lyrics: Lyrics? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerAlbumCoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.playerAlbumCoverViewpager
            .addOnPageChangeListener(this)
        binding.playerAlbumCoverViewpager
            .setPageTransformer(true, ParallaxPageTransformer())

        playerViewModel.queue.observe(viewLifecycleOwner, {
            binding.playerAlbumCoverViewpager.adapter =
                BaseCoverPagerAdapter(childFragmentManager, it)
            binding.playerAlbumCoverViewpager.currentItem = position
            onPageSelected(position)
        })
        playerViewModel.position.observe(viewLifecycleOwner, {
            binding.playerAlbumCoverViewpager.setCurrentItem(it, true)
        })
        playerViewModel.progress.observe(viewLifecycleOwner, {
            updateLyricsProgress(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.playerAlbumCoverViewpager.removeOnPageChangeListener(this)
        _binding = null
    }


    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        if (position != MusicPlayerRemote.position) {
            MusicPlayerRemote.position = position
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}
    private val isLyricsLayoutVisible: Boolean
        get() = lyrics != null && lyrics!!.isSynchronized && lyrics!!.isValid && synchronizedLyricsShow()

    private fun hideLyricsLayout() {
        binding.playerLyrics.animate().alpha(0f).setDuration(VISIBILITY_ANIM_DURATION.toLong())
            .withEndAction {
                if (_binding == null) return@withEndAction
                binding.playerLyrics.visibility = View.GONE
                binding.playerLyricsLine1.text = null
                binding.playerLyricsLine2.text = null
            }
    }

    fun setLyrics(l: Lyrics?) {
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