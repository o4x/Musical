/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.o4x.musical.imageloader.glide.module.artistimage

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.data.HttpUrlFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.o4x.musical.App
import com.o4x.musical.model.Artist
import com.o4x.musical.network.ApiClient
import com.o4x.musical.network.Models.DeezerArtistModel
import com.o4x.musical.network.service.DeezerService
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil
import java.io.IOException
import java.io.InputStream
import java.util.*

class ArtistImage(val artist: Artist) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ArtistImage
        return artist.id == that.artist.id &&
                artist.name == that.artist.name
    }

    override fun hashCode(): Int {
        return Objects.hash(artist.name) + artist.id.hashCode()
    }
}

class ArtistImageFetcher(
    private val deezerService: DeezerService,
    val model: ArtistImage,
    val width: Int,
    val height: Int
) : DataFetcher<InputStream> {

    private var urlFetcher: DataFetcher<InputStream>? = null
    private var isCancelled: Boolean = false

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        if (PreferenceUtil.isAllowedToDownloadMetadata(App.getContext())) {
            val artists = model.artist.name.split(",")
            val response = deezerService.searchDeezerArtist(artists[0]).execute()

            if (!response.isSuccessful) {
                throw IOException("Request failed with code: " + response.code())
            }

            if (isCancelled) return callback.onDataReady(null)

            try {
                val deezerResponse = response.body()
                val imageUrl = deezerResponse?.data?.get(0)?.let { getHighestQuality(it) }
                // Fragile way to detect a place holder image returned from Deezer:
                // ex: "https://e-cdns-images.dzcdn.net/images/artist//250x250-000000-80-0-0.jpg"
                // the double slash implies no artist identified
                val placeHolder = imageUrl?.contains("/images/artist//") ?: false
                if (!placeHolder) {
                    loadUrl(imageUrl.toString(), priority, callback)
                }
            } catch (e: Exception) {
                callback.onLoadFailed(e)
            }
        } else callback.onDataReady(null)
    }

    override fun cleanup() {
        urlFetcher?.cleanup()
    }

    override fun cancel() {
        isCancelled = true
        urlFetcher?.cancel()
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }

    private fun getHighestQuality(imageUrl: DeezerArtistModel.Data): String {
        return when {
            imageUrl.pictureXl.isNotEmpty() -> imageUrl.pictureXl
            imageUrl.pictureBig.isNotEmpty() -> imageUrl.pictureBig
            imageUrl.pictureMedium.isNotEmpty() -> imageUrl.pictureMedium
            imageUrl.pictureSmall.isNotEmpty() -> imageUrl.pictureSmall
            imageUrl.picture.isNotEmpty() -> imageUrl.picture
            else -> ""
        }
    }

    private fun loadUrl(
        url: String,
        priority: Priority,
        callback: DataFetcher.DataCallback<in InputStream>
    ) {
        val urlFetcher = HttpUrlFetcher(
            GlideUrl(url),
            TIMEOUT
        )
        urlFetcher.loadData(priority, callback)
    }

    companion object {
        // we need these very low values to make sure our artist image loading calls doesn't block the image loading queue
        private const val TIMEOUT: Int = 700
    }
}

class ArtistImageLoader(
    private val deezerService: DeezerService,
) : ModelLoader<ArtistImage, InputStream> {

    override fun buildLoadData(
        model: ArtistImage,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(
            ObjectKey(model.artist.name),
            ArtistImageFetcher(deezerService, model, width, height)
        )
    }

    override fun handles(model: ArtistImage): Boolean {
        return !MusicUtil.isArtistNameUnknown(model.artist.name)
    }
}

class ArtistImageFactory : ModelLoaderFactory<ArtistImage, InputStream> {

    private var deezerService: DeezerService =
        ApiClient.getClient(App.getContext()).create(DeezerService::class.java)

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ArtistImage, InputStream> {
        return ArtistImageLoader(deezerService)
    }

    override fun teardown() {}
}
