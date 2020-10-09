package com.o4x.musical.imageloader.universalil.loader

import android.content.Context
import android.widget.ImageView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.utils.L
import com.nostra13.universalimageloader.utils.MemoryCacheUtils
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.model.MultiImage
import com.o4x.musical.imageloader.universalil.listener.AbsImageLoadingListener
import com.o4x.musical.imageloader.universalil.listener.AbsImageLoadingListener.CoverData
import com.o4x.musical.imageloader.universalil.othersource.CustomImageDownloader
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Genre
import com.o4x.musical.model.Song
import com.o4x.musical.util.CoverUtil
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil


class UniversalIL(
    val image: ImageView,
    val listener: AbsImageLoadingListener = AbsImageLoadingListener(),
    val size: Int = DEFAULT_SIZE
) {

    constructor(image: ImageView, listener: AbsImageLoadingListener = AbsImageLoadingListener()) :
            this(image, listener, DEFAULT_SIZE)

    constructor(image: ImageView) :
            this(image, size= DEFAULT_SIZE)

    private fun displayImage(uri: String, options: DisplayImageOptions.Builder) {
        imageLoader?.displayImage(
            uri,
            image,
            options
                .build(),
            listener
        )
    }

    fun loadImage(
        song: Song,
    ) {

        if (PreferenceUtil.isIgnoreMediaStore()) {
            loadImage(AudioFileCover(song.title, song.data))
        } else {
            listener.setCoverData(
                CoverData(song.albumId, image, song.albumName, size)
            )

            displayImage(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId).toString(), options)
        }
    }

    fun loadImage(
        album: Album
    ) {
        loadImage(album.safeGetFirstSong())
    }

    fun loadImage(
        audioFileCover: AudioFileCover
    ) {

        listener.setCoverData(
            CoverData(
                audioFileCover.hashCode().toLong(), image, audioFileCover.title, size)
        )

        val uri = CustomImageDownloader.SCHEME_AUDIO + audioFileCover.filePath
        displayImage(uri,
            options
                .extraForDownloader(audioFileCover))
    }


    private fun loadImage(
        customImageUtil: CustomImageUtil,
        multiImage: MultiImage,
    ) {

        listener.setCoverData(
            CoverData(multiImage.id, image, multiImage.name, size)
        )

        if (customImageUtil.hasCustomImage()) {
            displayImage(
                customImageUtil.path,
                options
            )

        } else {
            val uri = CustomImageDownloader.SCHEME_MULTI + multiImage.hashCode()
            displayImage(
                uri,
                options
                    .extraForDownloader(multiImage),
            )
        }
    }


    fun loadImage(
        artist: Artist,
    ) {
        loadImage(
            CustomImageUtil(artist),
            MultiImage.fromArtist(artist))
    }

    fun loadImage(
        genre: Genre
    ) {
        loadImage(
            CustomImageUtil(genre),
            MultiImage.fromGenre(genre))
    }


    fun loadImage(
        url: String,
        name: String,
    ) {

        listener.setCoverData(
            CoverData(url.hashCode().toLong(), image, name, size)
        )

        displayImage(
            url,
            DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
        )
    }


    companion object {

        private const val DEFAULT_SIZE = CoverUtil.DEFAULT_SIZE

        private val options: DisplayImageOptions.Builder
            get() = DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .cacheInMemory(true)


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