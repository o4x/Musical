package com.o4x.musical.imageloader.glide.loader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.o4x.musical.R
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.model.CoverData
import com.o4x.musical.imageloader.model.MultiImage
import com.o4x.musical.imageloader.util.CustomCoverUtil
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Genre
import com.o4x.musical.model.Song
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil

class GlideLoader {

    companion object {

        val DEFAULT_DISK_CACHE_STRATEGY: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC
        const val DEFAULT_PLACEHOLDER_IMAGE: Int = R.drawable.default_album_art


        @JvmStatic
        fun with(context: Context): GlideManager {
            return GlideManager(context, GlideApp.with(context))
        }
    }

    class GlideManager(
        private val context: Context,
        private val requestManager: RequestManager
    ) {

        fun asBitmap(): GlideBuilder<Bitmap> {
            return GlideBuilder<Bitmap>(context, requestManager.asBitmap())
        }

        fun asDrawable(): GlideBuilder<Drawable> {
            return GlideBuilder<Drawable>(context, requestManager.asDrawable())
        }
    }

    class GlideBuilder<T>(private val context: Context) {

        private lateinit var requestBuilder: RequestBuilder<T>
        private var size: Int? = null

        constructor(context: Context, requestBuilder: RequestBuilder<T>) : this(context) {
            this@GlideBuilder.requestBuilder =
                requestBuilder
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
//                    .placeholder(DEFAULT_PLACEHOLDER_IMAGE)
        }

        fun withSize(size: Int): GlideBuilder<T> {
            this.size = size
            return this
        }

        private fun RequestBuilder<T>.setupOnError(coverData: CoverData): RequestBuilder<T> {

            coverData.context = context
            size?.let { coverData.size = it }


            return this
//                .placeholder(
//                    BitmapDrawable(context.resources, CustomCoverUtil.createCustomCover(coverData))
//                )
                .error(
                requestBuilder.clone()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .load(coverData)
                )

        }

        fun load(song: Song): RequestBuilder<T> {
            return if (PreferenceUtil.isIgnoreMediaStore()) {
                load(AudioFileCover(song.title, song.data))
            } else {
                requestBuilder
                    .load(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId))
                    .setupOnError(CoverData(song.id, song.title))
            }
        }

        fun load(album: Album): RequestBuilder<T> {
            return load(album.safeGetFirstSong())
        }

        fun load(audioFileCover: AudioFileCover): RequestBuilder<T> {
            return requestBuilder
                .load(audioFileCover)
                .setupOnError(CoverData(audioFileCover.hashCode().toLong(), audioFileCover.title))
        }

        private fun load(customImageUtil: CustomImageUtil, multiImage: MultiImage): RequestBuilder<T> {

            val coverData = CoverData(multiImage.id, multiImage.name)

            return if (customImageUtil.hasCustomImage()) {
                requestBuilder
                    .load(customImageUtil.file)
                    .setupOnError(coverData)
            } else {
                requestBuilder
                    .load(multiImage)
                    .setupOnError(coverData)
            }
        }

        fun load(artist: Artist): RequestBuilder<T> {
            return load(
                CustomImageUtil(artist),
                MultiImage.fromArtist(artist)
            )
        }

        fun load(genre: Genre): RequestBuilder<T> {
            return load(
                CustomImageUtil(genre),
                MultiImage.fromGenre(genre)
            )
        }

        fun load(
            url: String,
            name: String,
        ): RequestBuilder<T> {
            return requestBuilder
                .load(url)
                .setupOnError(CoverData(url.hashCode().toLong(), name))
        }

    }
}