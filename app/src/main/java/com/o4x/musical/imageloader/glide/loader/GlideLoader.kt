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
import com.o4x.musical.drawables.CharCoverDrawable
import com.o4x.musical.drawables.CoverData
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.imageloader.glide.module.artistimage.ArtistImage
import com.o4x.musical.imageloader.glide.targets.CustomBitmapTarget
import com.o4x.musical.imageloader.glide.targets.PalettableImageTarget
import com.o4x.musical.imageloader.glide.targets.palette.AbsPaletteTargetListener
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.model.MultiImage
import com.o4x.musical.model.*
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil


class GlideLoader {

    companion object {

        @JvmStatic
        fun with(context: Context): GlideBuilder {
            return GlideBuilder(context, GlideApp.with(context).asBitmap())
        }
    }

    class GlideBuilder(val context: Context, requestBuilder: RequestBuilder<Bitmap>) {

        private var listener = AbsPaletteTargetListener(context)

        private var requestBuilder: RequestBuilder<Bitmap> = requestBuilder
            .diskCacheStrategy(
                if (PreferenceUtil.isCacheImages())
                    DiskCacheStrategy.AUTOMATIC
                else
                    DiskCacheStrategy.NONE
            )
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//            .priority(Priority.LOW)

        fun withListener(listener: AbsPaletteTargetListener?): GlideBuilder {
            if (listener == null) {
                this.listener = AbsPaletteTargetListener(context)
            } else {
                this.listener = listener
            }
            return this
        }

        fun load(song: Song): GlideFinisher {
            return if (PreferenceUtil.isIgnoreMediaStore()) {
                load(AudioFileCover(song.albumName, song.data, song.dateModified))
            } else {

                GlideFinisher(
                    requestBuilder
                        .signature(createSignature(song))
                        .load(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId)),
                    CoverData.from(song),
                    listener
                )
            }
        }

        fun load(album: Album): GlideFinisher {
            return load(album.safeGetFirstSong())
        }

        fun load(audioFileCover: AudioFileCover): GlideFinisher {

            return GlideFinisher(
                requestBuilder
                    .load(audioFileCover),
                CoverData.from(audioFileCover),
                listener
            )
        }

        private fun load(customImageUtil: CustomImageUtil, multiImage: MultiImage): GlideFinisher {

            val coverData = CoverData.from(multiImage)

            return if (customImageUtil.hasCustomImage()) {
                GlideFinisher(
                    requestBuilder
                        .signature(createSignature(customImageUtil))
                        .load(customImageUtil.file),
                    coverData,
                    listener
                )
            } else {
                GlideFinisher(
                    requestBuilder
                        .load(multiImage),
                    coverData,
                    listener
                )
            }
        }

        fun load(artist: Artist): GlideFinisher {

            val coverData = CoverData.from(MultiImage.fromArtist(artist))

            val customImageUtil = CustomImageUtil(artist)

            return if (customImageUtil.hasCustomImage())
                GlideFinisher(
                    requestBuilder
                        .signature(createSignature(customImageUtil))
                        .load(customImageUtil.file),
                    coverData,
                    listener
                )
            else
                GlideFinisher(
                    requestBuilder
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .load(ArtistImage(artist)),
                    coverData,
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

            return GlideFinisher(
                requestBuilder
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .load(url),
                CoverData.from(url, name),
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

    @SuppressLint("CheckResult")
    class GlideFinisher(
        private val requestBuilder: RequestBuilder<Bitmap>,
        coverData: CoverData?,
        val listener: AbsPaletteTargetListener
    ) {

        init {

            coverData?.let {
                requestBuilder
                    .placeholder(
                        CharCoverDrawable(it)
                    )
            }
        }

        fun into(image: ImageView?): PalettableImageTarget? {
            image?.let {
                return requestBuilder
                    .into(
                        PalettableImageTarget(it).setListener(listener)
                    )
            }
            return null
        }

        fun into(target: CustomBitmapTarget): CustomBitmapTarget {
            return requestBuilder
                .into(target.setListener(listener))
        }
    }
}