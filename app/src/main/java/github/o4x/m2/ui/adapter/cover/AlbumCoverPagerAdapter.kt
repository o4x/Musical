package github.o4x.m2.ui.adapter.cover

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import github.o4x.m2.App
import github.o4x.m2.imageloader.glide.loader.GlideLoader
import github.o4x.m2.imageloader.glide.module.GlideApp
import github.o4x.m2.imageloader.glide.targets.CustomBitmapTarget
import github.o4x.m2.imageloader.glide.targets.palette.AbsPaletteTargetListener
import github.o4x.m2.imageloader.glide.transformation.blur.BlurTransformation
import github.o4x.m2.model.Song
import github.o4x.m2.util.Util

class AlbumCoverPagerAdapter(fm: FragmentManager, dataSet: List<Song>) :
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
                                .transition(BitmapTransitionOptions.withCrossFade(300)) // Added ms duration for smoother transition
                                // Shrink the massive Bitmap before blurring it
                                // 300x300 is plenty of detail for a blurred background
                                .override(300, 300)

                                // Optimize the blur calculation
                                // 25 is the max hardware-accelerated radius in Android
                                // 4 is the sampling rate (downscales the image 4x during calculation)
                                .transform(BlurTransformation(25, 4))

                                .load(resource)
                                .into(it)
                        }
                    }
                })
                .load(song)
                // We leave this at full screen size so the Front Pager can reuse this cached Bitmap instantly
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
