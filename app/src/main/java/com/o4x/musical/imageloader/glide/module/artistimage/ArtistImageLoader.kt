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

import android.content.Context
import com.bumptech.glide.Priority
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import com.o4x.musical.model.Artist
import com.o4x.musical.network.ApiClient
import com.o4x.musical.network.models.DeezerArtistModel
import com.o4x.musical.network.service.DeezerService
import com.o4x.musical.prefs.PreferenceUtil
import com.o4x.musical.util.MusicUtil
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

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
    val context: Context,
    private val deezerService: DeezerService,
    private val okHttp: OkHttpClient,
    val model: ArtistImage,
    val width: Int,
    val height: Int
) : DataFetcher<InputStream> {

    private var isCancelled: Boolean = false
    private var call: Call<DeezerArtistModel>? = null
    private var streamFetcher: OkHttpStreamFetcher? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        if (PreferenceUtil.isAllowedToDownloadMetadata(context)) {
            val artists = model.artist.name.split(",")
            call = deezerService.searchDeezerArtist(artists[0])

            call?.enqueue(object : Callback<DeezerArtistModel> {
                override fun onResponse(
                    call: Call<DeezerArtistModel>,
                    response: Response<DeezerArtistModel>
                ) {
                    if (response.isSuccessful) {
                        if (isCancelled) return callback.onDataReady(null)

                        try {
                            val deezerResponse = response.body()
                            val imageUrl =
                                deezerResponse?.data?.get(0)?.let { getHighestQuality(it) }
                            // Fragile way to detect a place holder image returned from Deezer:
                            // ex: "https://e-cdns-images.dzcdn.net/images/artist//250x250-000000-80-0-0.jpg"
                            // the double slash implies no artist identified
                            val placeHolder = imageUrl?.contains("/images/artist//") ?: false
                            if (!placeHolder) {
                                streamFetcher =
                                    OkHttpStreamFetcher(okHttp, GlideUrl(imageUrl.toString()))
                                streamFetcher!!.loadData(priority, callback)
                            }
                        } catch (e: Exception) {
                            callback.onLoadFailed(e)
                        }
                    }
                }

                override fun onFailure(call: Call<DeezerArtistModel>, t: Throwable) {
                    callback.onDataReady(null)
                }

            })
        } else callback.onDataReady(null)
    }

    override fun cleanup() {
        streamFetcher?.cleanup()
    }

    override fun cancel() {
        isCancelled = true
        streamFetcher?.cancel()
        call?.cancel()
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
}

class ArtistImageLoader(
    val context: Context,
    private val deezerService: DeezerService,
    private val okHttp: OkHttpClient
) : ModelLoader<ArtistImage, InputStream> {

    override fun buildLoadData(
        model: ArtistImage,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        return ModelLoader.LoadData(
            ObjectKey(model.artist.name),
            ArtistImageFetcher(context, deezerService, okHttp, model, width, height)
        )
    }

    override fun handles(model: ArtistImage): Boolean {
        return !MusicUtil.isArtistNameUnknown(model.artist.name)
    }
}

class ArtistImageFactory(val context: Context)
    : ModelLoaderFactory<ArtistImage, InputStream> {

    companion object {
        // we need these very low values to make sure our artist image loading calls doesn't block the image loading queue
        private const val TIMEOUT: Long = 700
    }

    private val deezerService: DeezerService =
        ApiClient.getClient(context).create(DeezerService::class.java)

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
        .build()

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ArtistImage, InputStream> {
        return ArtistImageLoader(context, deezerService, okHttp)
    }

    override fun teardown() {}
}
