package com.o4x.musical.imageloader.glide.loader

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.MediaStoreSignature
import com.bumptech.glide.signature.ObjectKey
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
import java.lang.Exception


class GlideLoader {

    companion object {

        val DEFAULT_DISK_CACHE_STRATEGY: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC
        const val DEFAULT_PLACEHOLDER_IMAGE: Int = R.drawable.default_album_art


        @JvmStatic
        fun with(context: Context): GlideBuilder {
            return GlideBuilder(GlideApp.with(context).asBitmap())
        }
    }

    class GlideBuilder() {

        private lateinit var requestBuilder: RequestBuilder<Bitmap>
        private var listener = PaletteTargetListener()

        constructor(requestBuilder: RequestBuilder<Bitmap>) : this() {
            this@GlideBuilder.requestBuilder =
                requestBuilder
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                    .priority(Priority.LOW)
//                    .placeholder(DEFAULT_PLACEHOLDER_IMAGE)
        }

        fun withListener(listener: PaletteTargetListener): GlideBuilder {
            this.listener = listener
            return this
        }

        fun load(song: Song): GlideFinisher {
            return if (PreferenceUtil.isIgnoreMediaStore()) {
                load(AudioFileCover(song.albumName, song.data, song.dateModified))
            } else {

                listener.coverData = CoverData.from(song)

                GlideFinisher(
                    requestBuilder
                        .signature(createSignature(song))
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
                        .signature(createSignature(customImageUtil))
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
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .load(url),
                listener
            )
        }

        private fun createSignature(song: Song): Key {
            return MediaStoreSignature("", song.dateModified, 0)
        }

        private fun createSignature(customImageUtil: CustomImageUtil): Key {
            return ObjectKey(customImageUtil.file.lastModified())
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

        fun into(target: Target<Bitmap>) {
            requestBuilder
                .into(target)
        }

        fun intoSync(image: ImageView) {
            image.setImageBitmap(
                createSync(image.context)
            )
        }

        fun createSync(context: Context): Bitmap {
            var bitmap: Bitmap? = null

            val thread = Thread {
                bitmap = try {
                    requestBuilder
                        .submit()
                        .get()
                } catch(e: Exception) {
                    listener.coverData.create(context)
                }
            }

            thread.start()
            thread.join()

            listener.isSync = true
            listener.onResourceReady(bitmap)

            return bitmap!!
        }
    }
}