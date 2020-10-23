package com.o4x.musical.imageloader.glide.loader

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.o4x.musical.R
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.imageloader.glide.targets.BitmapPaletteTarget
import com.o4x.musical.imageloader.glide.targets.PaletteTargetListener
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.model.CoverData
import com.o4x.musical.imageloader.model.MultiImage
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Genre
import com.o4x.musical.model.Song
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil


class GlideLoader {

    companion object {

        val DEFAULT_DISK_CACHE_STRATEGY: DiskCacheStrategy = DiskCacheStrategy.NONE
        const val DEFAULT_PLACEHOLDER_IMAGE: Int = R.drawable.default_album_art


        @JvmStatic
        fun with(context: Context): GlideBuilder {
            return GlideBuilder(context, GlideApp.with(context).asBitmap())
        }
    }

    class GlideBuilder(private val context: Context) {

        private lateinit var requestBuilder: RequestBuilder<Bitmap>
        private var listener = PaletteTargetListener()

        constructor(context: Context, requestBuilder: RequestBuilder<Bitmap>) : this(context) {
            this@GlideBuilder.requestBuilder =
                requestBuilder
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
//                    .placeholder(DEFAULT_PLACEHOLDER_IMAGE)
        }

        fun withListener(listener: PaletteTargetListener): GlideBuilder {
            this.listener = listener
            return this
        }

        fun load(song: Song): GlideFinisher {
            return if (PreferenceUtil.isIgnoreMediaStore()) {
                load(AudioFileCover(song.albumName, song.data))
            } else {

                listener.coverData = CoverData.from(song)

                GlideFinisher(
                    requestBuilder
                        .load(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId)),
                    listener
                )
            }
        }

        fun load(album: Album): GlideFinisher {
            return load(album.safeGetFirstSong())
        }

        fun load(audioFileCover: AudioFileCover): GlideFinisher {

            listener.coverData = CoverData.from(audioFileCover)

            return GlideFinisher(
                requestBuilder
                    .load(audioFileCover),
                listener
            )
        }

        private fun load(customImageUtil: CustomImageUtil, multiImage: MultiImage): GlideFinisher {

            listener.coverData = CoverData.from(multiImage)

            return if (customImageUtil.hasCustomImage()) {
                GlideFinisher(
                    requestBuilder
                        .load(customImageUtil.file),
                    listener
                )
            } else {
                GlideFinisher(
                    requestBuilder
                        .load(multiImage),
                    listener
                )
            }
        }

        fun load(artist: Artist): GlideFinisher {
            return load(
                CustomImageUtil(artist),
                MultiImage.fromArtist(artist)
            )
        }

        fun load(genre: Genre): GlideFinisher {
            return load(
                CustomImageUtil(genre),
                MultiImage.fromGenre(genre)
            )
        }

        fun load(
            url: String,
            name: String,
        ): GlideFinisher {

            listener.coverData = CoverData.from(url, name)

            return GlideFinisher(
                requestBuilder
                    .load(url),
                listener
            )
        }
    }

    class GlideFinisher(
        private val requestBuilder: RequestBuilder<Bitmap>,
        private val listener: PaletteTargetListener,
    ) {

        fun withSize(size: Int): GlideFinisher {
            listener.coverData.size = size
            return this
        }

        fun into(image: ImageView) {
            requestBuilder
                .into(
                    BitmapPaletteTarget(image, listener)
                )
        }

        fun intoSync(image: ImageView) {
            var bitmap: Bitmap? = null

            val thread = Thread {
                 bitmap = requestBuilder
                    .error(
                        requestBuilder.clone()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .load(listener.coverData)
                    )
                    .submit()
                    .get()
            }

            thread.start()
            thread.join()

            image.setImageBitmap(bitmap)
        }
    }
}