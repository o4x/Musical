package com.o4x.musical.network

import android.content.Context
import com.google.gson.GsonBuilder
import com.o4x.musical.App
import com.o4x.musical.BuildConfig
import com.o4x.musical.network.service.DeezerService
import com.o4x.musical.network.service.LastFMService
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit


fun provideDefaultCache(): Cache? {
    val cacheDir = File(App.getContext().cacheDir.absolutePath, "/okhttp-lastfm/")
    if (cacheDir.mkdirs() || cacheDir.isDirectory) {
        return Cache(cacheDir, 1024 * 1024 * 10)
    }
    return null
}

fun logInterceptor(): Interceptor {
    val loggingInterceptor = HttpLoggingInterceptor()
    if (BuildConfig.DEBUG) {
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    } else {
        // disable retrofit log on release
        loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
    }
    return loggingInterceptor
}

fun headerInterceptor(context: Context): Interceptor {
    return Interceptor {
        val original = it.request()
        val request = original.newBuilder()
            .header("User-Agent", context.packageName)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .method(original.method, original.body)
            .build()
        it.proceed(request)
    }
}

fun provideOkHttp(context: Context, cache: Cache): OkHttpClient {
    return OkHttpClient.Builder()
        .addNetworkInterceptor(logInterceptor())
        //.addInterceptor(headerInterceptor(context))
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .cache(cache)
        .build()
}

fun provideLastFmRetrofit(client: OkHttpClient): Retrofit {
    val gson = GsonBuilder()
        .setLenient()
        .create()
    return Retrofit.Builder()
        .baseUrl("https://ws.audioscrobbler.com/2.0/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory(object: Call.Factory {
            override fun newCall(request: Request): Call {
                return client.newCall(request)
            }
        })
        .build()
}

fun provideLastFmRest(retrofit: Retrofit): LastFMService {
    return retrofit.create(LastFMService::class.java)
}

fun provideDeezerRest(retrofit: Retrofit): DeezerService {
    val newBuilder = retrofit.newBuilder()
        .baseUrl("https://api.deezer.com/")
        .build()
    return newBuilder.create(DeezerService::class.java)
}

//fun provideLyrics(retrofit: Retrofit): LyricsRestService {
//    val newBuilder = retrofit.newBuilder()
//        .baseUrl("https://makeitpersonal.co")
//        .addConverterFactory(LyricsConverterFactory())
//        .build()
//    return newBuilder.create(LyricsRestService::class.java)
//}