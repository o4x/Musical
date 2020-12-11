package com.o4x.musical.ui.adapter.cover

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.o4x.musical.App
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.imageloader.glide.targets.CustomBitmapTarget
import com.o4x.musical.imageloader.glide.targets.palette.AbsPaletteTargetListener
import com.o4x.musical.imageloader.glide.transformation.blur.BlurTransformation
import com.o4x.musical.model.Song
import com.o4x.musical.util.Util
import com.o4x.musical.util.color.MediaNotificationProcessor

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class AlbumCoverPagerAdapter(fm: FragmentManager?, dataSet: List<Song>) :
    BaseCoverPagerAdapter(fm, dataSet) {

    override fun getItem(position: Int): Fragment {
        return AlbumCoverFragment.newInstance(dataSet[position])
    }

    class AlbumCoverFragment : BaseCoverFragment() {

        override fun loadAlbumCover() {
            GlideLoader.with(requireContext())
                .withListener(object : AbsPaletteTargetListener(requireContext()) {
                    override fun onResourceReady(resource: Bitmap?) {
                        super.onResourceReady(resource)
                        if (resource == null) return
                        _binding?.playerImage?.let {
                            GlideApp.with(App.getContext())
                                .asBitmap()
                                .transition(BitmapTransitionOptions.withCrossFade())
                                .transform(BlurTransformation(100, 1))
                                .load(resource)
                                .into(it)
                        }
                    }
                }.also { it.loadPlaceholderPalette = true })
                .withBlur(100f)
                .load(song)
                .into(CustomBitmapTarget(Util.getScreenWidth(), Util.getScreenHeight()))
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