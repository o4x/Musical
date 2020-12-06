package com.o4x.musical.imageloader.glide.loader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
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
import com.o4x.musical.imageloader.glide.transformation.blur.BlurTransformation
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.model.MultiImage
import com.o4x.musical.model.*
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.prefs.PreferenceUtil


class GlideLoader {

    companion object {

        @JvmStatic
        fun with(context: Context): GlideBuilder {
            return GlideBuilder(context, GlideApp.with(context).asBitmap())
        }
    }

    @SuppressLint("CheckResult")
    class GlideBuilder(val context: Context, requestBuilder: RequestBuilder<Bitmap>) {

        private var listener = AbsPaletteTargetListener(context)
        private var radius: Float? = null

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


        fun withBlur(radius: Float): GlideBuilder {
            this.radius = radius
            return this
        }

        fun load(song: Song): GlideFinisher {
            return if (PreferenceUtil.isIgnoreMediaStore()) {
                load(AudioFileCover(song.albumName, song.data, song.dateModified))
            } else {

                requestBuilder
                    .signature(createSignature(song))
                    .load(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId))

                GlideFinisher(
                    CoverData.from(song)
                )
            }
        }

        fun load(album: Album): GlideFinisher {
            return load(album.safeGetFirstSong())
        }

        fun load(audioFileCover: AudioFileCover): GlideFinisher {

            requestBuilder
                .load(audioFileCover)

            return GlideFinisher(
                CoverData.from(audioFileCover)
            )
        }

        private fun load(customImageUtil: CustomImageUtil, multiImage: MultiImage): GlideFinisher {

            val coverData = CoverData.from(multiImage)

            return if (customImageUtil.hasCustomImage()) {
                requestBuilder
                    .signature(createSignature(customImageUtil))
                    .load(customImageUtil.file)

                GlideFinisher(coverData)
            } else {
                requestBuilder
                    .load(multiImage)

                GlideFinisher(coverData)
            }
        }

        fun load(artist: Artist): GlideFinisher {

            val coverData = CoverData.from(MultiImage.fromArtist(artist))

            val customImageUtil = CustomImageUtil(artist)

            return if (customImageUtil.hasCustomImage()) {
                requestBuilder
                    .signature(createSignature(customImageUtil))
                    .load(customImageUtil.file)

                GlideFinisher(coverData)

            } else {
                requestBuilder
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .load(ArtistImage(artist))

                GlideFinisher(coverData)
            }

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

            requestBuilder
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(url)

            return GlideFinisher(
                CoverData.from(url, name)
            )
        }

        fun load(
            @DrawableRes resource: Int
        ): GlideFinisher {

            requestBuilder
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(resource)

            return GlideFinisher()
        }

        fun load(
            uri: Uri
        ): GlideFinisher {

            requestBuilder
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(uri)

            return GlideFinisher()
        }

        private fun createSignature(song: Song): Key {
            return MediaStoreSignature("", song.dateModified, 0)
        }

        private fun createSignature(customImageUtil: CustomImageUtil): Key {
            return ObjectKey(customImageUtil.file.lastModified())
        }

        inner class GlideFinisher(
            coverData: CoverData? = null
        ) {

            init {
                radius?.let {
                    requestBuilder
                        .transform(BlurTransformation(it.toInt(), 1))
                }

                coverData?.let {

                    requestBuilder
                        .placeholder(
                            CharCoverDrawable(it).apply {
                                radius?.let { setBlur(it) }
                            }
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
}