package github.o4x.m2.ui.adapter.cover

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import github.o4x.m2.App
import github.o4x.m2.imageloader.glide.loader.GlideLoader
import github.o4x.m2.imageloader.glide.targets.CustomBitmapTarget
import github.o4x.m2.imageloader.glide.targets.palette.AbsPaletteTargetListener
import github.o4x.m2.imageloader.glide.transformation.blur.BlurTransformation
import github.o4x.m2.model.Song

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
                            Glide.with(App.getContext())
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
                // The only consumer of this bitmap is the blur below, which shrinks it
                // to 300x300 anyway, so decode it small. Decoding at full screen size
                // here allocated a ~10MB bitmap per page whose detail was immediately
                // thrown away — and that decode landed while ViewPager prefetched the
                // next page mid-skip, causing the swipe animation to stutter.
                .into(CustomBitmapTarget(BLUR_SOURCE_SIZE, BLUR_SOURCE_SIZE))
        }

        companion object {
            private const val SONG_ARG = "song"
            // Source size for the blurred background. The blur pass downscales to
            // 300x300, so anything at or above that is plenty of detail.
            private const val BLUR_SOURCE_SIZE = 400
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
