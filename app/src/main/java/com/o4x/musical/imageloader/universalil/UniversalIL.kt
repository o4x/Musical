package com.o4x.musical.imageloader.universalil

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.utils.L
import com.nostra13.universalimageloader.utils.MemoryCacheUtils
import com.o4x.musical.imageloader.model.MultiImage
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.universalil.othersource.CustomImageDownloader
import com.o4x.musical.imageloader.universalil.palette.AbsImageLoadingListener
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Genre
import com.o4x.musical.model.Song
import com.o4x.musical.util.ColorCoverUtil
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.ColorCoverUtil.createSquareCoverWithText
import com.o4x.musical.util.MusicUtil

object UniversalIL {

    private const val DEFAULT_SIZE = ColorCoverUtil.DEFAULT_SIZE

    private val options: DisplayImageOptions.Builder
        get() = DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true)
            .cacheInMemory(true)

    var imageLoader: ImageLoader? = null
        private set

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

    @JvmStatic
    fun songImageLoader(
        song: Song,
        image: ImageView,
        listener: AbsImageLoadingListener?,
        size: Int = DEFAULT_SIZE
    ) {

        val b: Bitmap = createSquareCoverWithText(
            image.context, song.albumName, song.albumId, size)
        val d: Drawable = b.toDrawable(image.resources)
        listener?.setOnFailedBitmap(b)

        imageLoader?.displayImage(
            MusicUtil.getMediaStoreAlbumCoverUri(song.albumId).toString(),
            image,
            options
                .showImageOnFail(d)
                .showImageForEmptyUri(d)
                .build(),
            listener
        )
    }

    @JvmStatic
    fun audioFileImageLoader(
        audioFileCover: AudioFileCover,
        image: ImageView,
        listener: AbsImageLoadingListener?,
        size: Int = DEFAULT_SIZE
    ) {

        val b: Bitmap = createSquareCoverWithText(
            image.context, audioFileCover.title, audioFileCover.hashCode(), size)
        val d: Drawable = b.toDrawable(image.resources)
        listener?.setOnFailedBitmap(b)

        val imageId = CustomImageDownloader.SCHEME_AUDIO + audioFileCover.filePath
        imageLoader?.displayImage(
            imageId,
            image,
            options
                .extraForDownloader(audioFileCover)
                .showImageForEmptyUri(d)
                .showImageOnFail(d)
                .build(),
            listener
        )
    }


    @JvmStatic
    private fun multiImageLoader(
        customImageUtil: CustomImageUtil,
        multiImage: MultiImage,
        image: ImageView,
        listener: AbsImageLoadingListener? = null,
        size: Int = DEFAULT_SIZE
    ) {

        val b: Bitmap = createSquareCoverWithText(
            image.context, multiImage.name, multiImage.id, size)
        val d: Drawable = b.toDrawable(image.resources)
        listener?.setOnFailedBitmap(b)

        if (customImageUtil.hasCustomImage()) {
            imageLoader?.displayImage(
                customImageUtil.path,
                image,
                options
                    .showImageOnFail(d)
                    .showImageForEmptyUri(d)
                    .build(),
                listener
            )
        } else {
            val imageId = CustomImageDownloader.SCHEME_MULTI + multiImage.hashCode()
            imageLoader?.displayImage(
                imageId,
                image,
                options
                    .extraForDownloader(multiImage)
                    .showImageOnFail(d)
                    .showImageForEmptyUri(d)
                    .build(),
                listener
            )
        }
    }

    @JvmStatic
    fun artistImageLoader(
        artist: Artist,
        image: ImageView,
        listener: AbsImageLoadingListener? = null,
        size: Int = DEFAULT_SIZE,
    ) {
        multiImageLoader(
            CustomImageUtil(artist),
            MultiImage.fromArtist(artist),
            image,
            listener,
            size)
    }

    @JvmStatic
    fun genreImageLoader(
        genre: Genre,
        image: ImageView,
        listener: AbsImageLoadingListener?,
        size: Int = DEFAULT_SIZE
    ) {
        multiImageLoader(
            CustomImageUtil(genre),
            MultiImage.fromGenre(genre),
            image,
            listener,
            size)
    }

    @JvmStatic
    fun uriImageLoader(
        url: String,
        name: String,
        image: ImageView,
        listener: AbsImageLoadingListener?,
        size: Int = DEFAULT_SIZE
    ) {

        val b: Bitmap = createSquareCoverWithText(
            image.context, name, url.hashCode(), size)
        val d: Drawable = b.toDrawable(image.resources)
        listener?.setOnFailedBitmap(b)

        imageLoader?.displayImage(
            url,
            image,
            DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnFail(d)
                .showImageForEmptyUri(d)
                .build(),
            listener
        )
    }

    @JvmStatic
    fun removeFromCache(uri: String?) {
        MemoryCacheUtils.removeFromCache(uri, imageLoader?.memoryCache)
        val imageFile = imageLoader!!.diskCache[uri]
        if (imageFile != null && imageFile.exists()) {
            imageFile.delete()
        }
    }

    @JvmStatic
    fun songImageLoader(song: Song, image: ImageView, listener: AbsImageLoadingListener?)
        = songImageLoader(song, image, listener, DEFAULT_SIZE)

    @JvmStatic
    fun artistImageLoader(artist: Artist, image: ImageView, listener: AbsImageLoadingListener?)
        = artistImageLoader(artist, image, listener, DEFAULT_SIZE)

    @JvmStatic
    fun genreImageLoader(genre: Genre, image: ImageView, listener: AbsImageLoadingListener?)
            = genreImageLoader(genre, image, listener, DEFAULT_SIZE)

    @JvmStatic
    fun uriImageLoader(url: String, name: String, image: ImageView, listener: AbsImageLoadingListener?)
        = uriImageLoader(url, name, image, listener, DEFAULT_SIZE)

    @JvmStatic
    fun audioFileImageLoader(audioFileCover: AudioFileCover, image: ImageView, listener: AbsImageLoadingListener?)
        = audioFileImageLoader(audioFileCover, image, listener, DEFAULT_SIZE)
}