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
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.nostra13.universalimageloader.utils.L
import com.nostra13.universalimageloader.utils.MemoryCacheUtils
import com.o4x.musical.R
import com.o4x.musical.imageloader.model.ArtistImage
import com.o4x.musical.imageloader.model.AudioFileCover
import com.o4x.musical.imageloader.universalil.othersource.CustomImageDownloader
import com.o4x.musical.imageloader.universalil.palette.AbsImageLoadingListener
import com.o4x.musical.imageloader.universalil.palette.PaletteImageLoadingListener
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Song
import com.o4x.musical.util.ArtistImageUtil
import com.o4x.musical.util.ColorCoverUtil.createSquareCoverWithText
import com.o4x.musical.util.MusicUtil

object UniversalIL {

    private const val DEFAULT_ARTIST_IMAGE = R.drawable.default_artist_image
    private const val DEFAULT_ALBUM_IMAGE = R.drawable.default_album_art
    private const val DEFAULT_SIZE = 500

    private val options: DisplayImageOptions.Builder
        get() = DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true)
            .cacheInMemory(true)

    private val songOptions = options
        .showImageOnLoading(DEFAULT_ALBUM_IMAGE)
        .showImageOnFail(DEFAULT_ALBUM_IMAGE)
        .showImageForEmptyUri(DEFAULT_ALBUM_IMAGE)
        .build()

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
            image.context, song.title, song.hashCode(), size)
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
    fun artistImageLoader(
        artist: Artist,
        image: ImageView,
        listener: AbsImageLoadingListener?,
        size: Int = DEFAULT_SIZE
    ) {

        val b: Bitmap = createSquareCoverWithText(
            image.context, artist.name, artist.hashCode(), size)
        val d: Drawable = b.toDrawable(image.resources)
        listener?.setOnFailedBitmap(b)

        val hasCustomImage = ArtistImageUtil.hasCustomArtistImage(artist)
        if (hasCustomImage) {
            imageLoader?.displayImage(
                ArtistImageUtil.getPath(artist),
                image,
                options
                    .showImageOnLoading(d)
                    .showImageOnFail(d)
                    .showImageForEmptyUri(d)
                    .build(),
                listener
            )
        } else {
            val artistImage = ArtistImage.fromArtist(artist)
            val imageId = CustomImageDownloader.SCHEME_ARTIST + artistImage.hashCode()
            imageLoader?.displayImage(
                imageId,
                image,
                options
                    .extraForDownloader(artistImage)
                    .showImageOnLoading(DEFAULT_ARTIST_IMAGE)
                    .showImageOnFail(DEFAULT_ARTIST_IMAGE)
                    .showImageForEmptyUri(DEFAULT_ARTIST_IMAGE)
                    .build(),
                listener
            )
        }
    }

    @JvmStatic
    fun onlineAlbumImageLoader(
        url: String,
        image: ImageView,
        listener: AbsImageLoadingListener?,
        size: Int = DEFAULT_SIZE
    ) {

        val b: Bitmap = createSquareCoverWithText(
            image.context, "", url.hashCode(), size)
        val d: Drawable = b.toDrawable(image.resources)
        listener?.setOnFailedBitmap(b)

        imageLoader?.displayImage(
            url,
            image,
            DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(d)
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
        if (imageFile.exists()) {
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
    fun onlineAlbumImageLoader(url: String, image: ImageView, listener: AbsImageLoadingListener?)
        = onlineAlbumImageLoader(url, image, listener, DEFAULT_SIZE)

    @JvmStatic
    fun audioFileImageLoader(audioFileCover: AudioFileCover, image: ImageView, listener: AbsImageLoadingListener?)
        = audioFileImageLoader(audioFileCover, image, listener, DEFAULT_SIZE)
}