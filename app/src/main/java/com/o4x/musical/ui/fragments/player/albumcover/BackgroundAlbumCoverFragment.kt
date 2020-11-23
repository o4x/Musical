package com.o4x.musical.ui.fragments.player.albumcover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentPlayerAlbumCoverBinding
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.ui.adapter.cover.AlbumCoverPagerAdapter
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment

class BackgroundAlbumCoverFragment : AbsMusicServiceFragment(R.layout.fragment_player_album_cover),
    ViewPager.OnPageChangeListener {

    private var _binding: FragmentPlayerAlbumCoverBinding? = null
    private val binding get() = _binding!!

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
            .setPageTransformer(true, FadePageTransformer())

        playerViewModel.queue.observe(viewLifecycleOwner, {
            binding.playerAlbumCoverViewpager.adapter =
                AlbumCoverPagerAdapter(childFragmentManager, it)
            binding.playerAlbumCoverViewpager.currentItem = MusicPlayerRemote.position
            onPageSelected(MusicPlayerRemote.position)
        })
        playerViewModel.position.observe(viewLifecycleOwner, {
            binding.playerAlbumCoverViewpager.setCurrentItem(it, true)
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
}