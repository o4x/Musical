package com.o4x.musical.imageloader.glide.loader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.MediaStoreSignature
import com.bumptech.glide.signature.ObjectKey
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.imageloader.glide.module.artistimage.ArtistImage
import com.o4x.musical.imageloader.glide.targets.AbsBitmapPaletteTarget
import com.o4x.musical.imageloader.glide.targets.BitmapPaletteTarget
import com.o4x.musical.imageloader.glide.targets.AbsPaletteTargetListener
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.model.CoverData
import com.o4x.musical.imageloader.model.MultiImage
import com.o4x.musical.model.*
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil
import com.o4x.musical.views.SquareImageView


class GlideLoader {

    companion object {

        @JvmStatic
        fun with(context: Context): GlideBuilder {
            return GlideBuilder(GlideApp.with(context).asBitmap())
        }
    }

    class GlideBuilder() {

        private lateinit var requestBuilder: RequestBuilder<Bitmap>
        private var listener =
            AbsPaletteTargetListener()

        constructor(requestBuilder: RequestBuilder<Bitmap>) : this() {
            this@GlideBuilder.requestBuilder =
                requestBuilder
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                    .priority(Priority.LOW)
//                    .placeholder(R.drawable.default_album_art)
        }

        fun withListener(listenerAbs: AbsPaletteTargetListener?): GlideBuilder {
            listenerAbs?.let {
                this.listener = it
            }
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

            listener.coverData = CoverData.from(MultiImage.fromArtist(artist))

            val customImageUtil = CustomImageUtil(artist)

            return if (customImageUtil.hasCustomImage())
                GlideFinisher(
                    requestBuilder
                        .signature(createSignature(customImageUtil))
                        .load(customImageUtil.file),
                    listener
                )
            else
                GlideFinisher(
                    requestBuilder
                        .load(ArtistImage(artist)),
                    listener
                )
        }

        fun load(genre: Genre): GlideFinisher {
            return load(
                CustomImageUtil(genre),
                MultiImage.fromGenre(genre)
            )
        }

        fun load(playlist: Playlist, songs: List<Song>): GlideFinisher {
            return load(
                CustomImageUtil(playlist),
                MultiImage.fromPlaylist(playlist, songs)
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
        private val listener: AbsPaletteTargetListener,
    ) {

        fun withSize(size: Int): GlideFinisher {
            listener.coverData.size = size
            return this
        }

        fun into(image: ImageView?) {
            image?.let {
                requestBuilder
                    .into(
                        BitmapPaletteTarget(it, listener)
                    )
            }
        }

        fun into(image: SquareImageView?) {
            image?.let {
                it.coverData = listener.coverData

                requestBuilder
                    .into(AbsBitmapPaletteTarget(it, listener))
            }
        }

        fun into(target: Target<Bitmap>) {
            requestBuilder
                .into(target)
        }

        @SuppressLint("CheckResult")
        fun intoSync(image: ImageView, onlyRetrieveFromCache: Boolean = true) {

            requestBuilder.onlyRetrieveFromCache(onlyRetrieveFromCache)

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
                    e.printStackTrace()
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