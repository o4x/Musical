package com.o4x.musical.ui.adapter.cover

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.model.Song
import com.o4x.musical.util.color.MediaNotificationProcessor

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class AlbumCoverPagerAdapter(fm: FragmentManager?, dataSet: List<Song>) :
    BaseCoverPagerAdapter(fm, dataSet) {

    class AlbumCoverFragment : BaseCoverFragment() {


        private var isColorReady = false
        private var colors: MediaNotificationProcessor? = null
        private var request = 0

        override fun loadAlbumCover() {
            GlideLoader.with(requireContext())

                .withBlur(100f)
                .load(song)
                .into(binding.playerImage)
        }

        companion object {
            private const val SONG_ARG = "song"
            fun newInstance(song: Song?): AlbumCoverFragment {
                val frag = AlbumCoverFragment()
                val args = Bundle()
                args.putParcelable(SONG_ARG, song)
                frag.arguments = args
                return frag
            }
        }
    }
}