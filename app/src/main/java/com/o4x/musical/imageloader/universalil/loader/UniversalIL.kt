package com.o4x.musical.imageloader.universalil.loader

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.utils.L
import com.nostra13.universalimageloader.utils.MemoryCacheUtils
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.model.CoverData
import com.o4x.musical.imageloader.model.MultiImage
import com.o4x.musical.imageloader.universalil.listener.AbsImageLoadingListener
import com.o4x.musical.imageloader.universalil.othersource.CustomImageDownloader
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Genre
import com.o4x.musical.model.Song
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil


class UniversalIL {


    private var listener: AbsImageLoadingListener? = AbsImageLoadingListener()
    private var size: Int? = null

    fun withListener(listener: AbsImageLoadingListener): UniversalIL {
        this.listener = listener
        return this
    }

    fun withSize(size: Int): UniversalIL {
        this.size = size
        return this
    }


    inner class Builder(
        private val url: String,
        private val options: DisplayImageOptions.Builder
    ) {
        init {
            size?.let {
                listener?.coverData?.size = it
            }
        }

        fun displayInTo(image: ImageView) {

            listener?.coverData?.image = image

            imageLoader?.displayImage(
                url,
                image,
                options
                    .build(),
                listener
            )
        }

        fun loadImage(context: Context) {

            listener?.setContext(context)

            imageLoader?.loadImage(
                url,
                options
                    .build(),
                listener
            )
        }

        fun loadImageSync(image: ImageView): Bitmap? {
            var bitmap: Bitmap? = imageLoader?.loadImageSync(
                url,
                options
                    .build()
            )

            if (bitmap == null) {

                val coverData: CoverData = listener!!.coverData

                bitmap = coverData.create(image.context);
            }

            listener?.isLoadColorSync = true
            listener?.onLoadingStarted(url, image)
            listener?.onLoadingComplete(url, image, bitmap)

            image.setImageBitmap(bitmap)
            return bitmap
        }
    }

    fun byThis(song: Song): Builder {
        return if (PreferenceUtil.isIgnoreMediaStore()) {
            byThis(AudioFileCover(song.albumName, song.data))
        } else {
            listener?.coverData = CoverData.from(song)

            Builder(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId).toString(), options)
        }
    }

    fun byThis(
        album: Album
    ): Builder {
        return byThis(album.safeGetFirstSong())
    }

    fun byThis(
        audioFileCover: AudioFileCover
    ): Builder {

        listener?.coverData = CoverData.from(audioFileCover)

        val uri = CustomImageDownloader.SCHEME_AUDIO + audioFileCover.filePath
        return Builder(
            uri,
            options
                .extraForDownloader(audioFileCover)
        )
    }


    private fun byThis(
        customImageUtil: CustomImageUtil,
        multiImage: MultiImage,
    ): Builder {

        listener?.coverData = CoverData.from(multiImage)

        return if (customImageUtil.hasCustomImage()) {
            Builder(
                customImageUtil.path,
                options
            )

        } else {
            val uri = CustomImageDownloader.SCHEME_MULTI + multiImage.hashCode()
            Builder(
                uri,
                options
                    .extraForDownloader(multiImage),
            )
        }
    }


    fun byThis(
        artist: Artist,
    ): Builder {
        return byThis(
            CustomImageUtil(artist),
            MultiImage.fromArtist(artist)
        )
    }

    fun byThis(
        genre: Genre
    ): Builder {
        return byThis(
            CustomImageUtil(genre),
            MultiImage.fromGenre(genre)
        )
    }


    fun byThis(
        url: String,
        name: String,
    ): Builder {

        listener?.coverData = CoverData.from(url, name)

        return Builder(
            url,
            DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .imageScaleType(ImageScaleType.EXACTLY)
        )
    }


    companion object {

        private val options: DisplayImageOptions.Builder
            get() = DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .resetViewBeforeLoading(true)


        var imageLoader: ImageLoader? = null

        fun initImageLoader(context: Context) {
            imageLoader = ImageLoader.getInstance()
            val config = ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options.build())
                .diskCacheSize(1024 * 1024 * 100 /* 100MB */)
                .memoryCacheSize(1024 * 1024 * 300 /* 300MB */)
                .imageDownloader(CustomImageDownloader(context))
                .denyCacheImageMultipleSizesInMemory()
                .build()
            imageLoader?.init(config)
            L.writeDebugLogs(false)
            L.writeLogs(false)
        }


        fun removeFromCache(uri: String?) {
            MemoryCacheUtils.removeFromCache(uri, imageLoader?.memoryCache)
            val imageFile = imageLoader!!.diskCache[uri]
            if (imageFile != null && imageFile.exists()) {
                imageFile.delete()
            }
        }
    }
}