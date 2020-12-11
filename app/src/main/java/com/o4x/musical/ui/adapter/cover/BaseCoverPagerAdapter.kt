package com.o4x.musical.ui.adapter.cover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.o4x.musical.databinding.FragmentAlbumCoverBinding
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.misc.CustomFragmentStatePagerAdapter
import com.o4x.musical.model.Song

open class BaseCoverPagerAdapter(fm: FragmentManager?, var dataSet: List<Song>) :
    CustomFragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return BaseCoverFragment.newInstance(dataSet[position])
    }

    override fun getCount(): Int {
        return dataSet.size
    }

    open class BaseCoverFragment : Fragment() {

        var _binding: FragmentAlbumCoverBinding? = null
        val binding get() = _binding!!

        val song: Song by lazy { requireArguments().getParcelable(SONG_ARG)!! }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = FragmentAlbumCoverBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            loadAlbumCover()
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        open fun loadAlbumCover() {
            GlideLoader.with(requireContext())
                .load(song)
                .into(binding.playerImage)
        }

        companion object {
            private const val SONG_ARG = "song"
            fun newInstance(song: Song?): BaseCoverFragment {
                val frag = BaseCoverFragment()
                val args = Bundle()
                args.putParcelable(SONG_ARG, song)
                frag.arguments = args
                return frag
            }
        }
    }
}