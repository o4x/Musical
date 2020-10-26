/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.o4x.musical.helper

import android.annotation.TargetApi
import android.app.Activity
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.o4x.musical.R
import com.o4x.musical.model.Song
import com.o4x.musical.repository.SongRepository
import com.o4x.musical.service.MusicService
import com.o4x.musical.util.PreferenceUtil
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File
import java.util.*

object MusicPlayerRemote : KoinComponent {
    val TAG: String = MusicPlayerRemote::class.java.simpleName

    private val mConnectionMap = WeakHashMap<Context, ServiceBinder>()

    @JvmStatic
    var musicService: MusicService? = null

    private val songRepository by inject<SongRepository>()


    @JvmStatic
    val isPlaying: Boolean
        get() = musicService != null && musicService!!.isPlaying

    @JvmStatic
    fun isPlaying(song: Song): Boolean {
        return if (!isPlaying) {
            false
        } else song.id == currentSong.id
    }

    @JvmStatic
    val currentSong: Song
        get() = if (musicService != null) {
            musicService!!.currentSong
        } else Song.emptySong

    /**
     * Async
     */
    @JvmStatic
    var position: Int
        get() = if (musicService != null) {
            musicService!!.position
        } else -1
        set(position) {
            if (musicService != null) {
                musicService!!.position = position
            }
        }

    @JvmStatic
    val playingQueue: List<Song>
        get() = if (musicService != null) {
            musicService?.playingQueue as List<Song>
        } else listOf<Song>()

    @JvmStatic
    val songProgressMillis: Int
        get() = if (musicService != null) {
            musicService!!.songProgressMillis
        } else -1

    @JvmStatic
    val songDurationMillis: Int
        get() = if (musicService != null) {
            musicService!!.songDurationMillis
        } else -1

    @JvmStatic
    val repeatMode: Int
        get() = if (musicService != null) {
            musicService!!.repeatMode
        } else MusicService.REPEAT_MODE_NONE

    @JvmStatic
    val shuffleMode: Int
        get() = if (musicService != null) {
            musicService!!.shuffleMode
        } else MusicService.SHUFFLE_MODE_NONE

    @JvmStatic
    val audioSessionId: Int
        get() = if (musicService != null) {
            musicService!!.audioSessionId
        } else -1

    @JvmStatic
    val isServiceConnected: Boolean
        get() = musicService != null

    @JvmStatic
    fun bindToService(context: Context, callback: ServiceConnection): ServiceToken? {

        var realActivity: Activity? = (context as Activity).parent
        if (realActivity == null) {
            realActivity = context
        }

        val contextWrapper = ContextWrapper(realActivity)
        val intent = Intent(contextWrapper, MusicService::class.java)
        try {
            contextWrapper.startService(intent)
        } catch (ignored: IllegalStateException) {
            ContextCompat.startForegroundService(context, intent)
        }
        val binder = ServiceBinder(callback)

        if (contextWrapper.bindService(
                Intent().setClass(contextWrapper, MusicService::class.java),
                binder,
                Context.BIND_AUTO_CREATE
            )
        ) {
            mConnectionMap[contextWrapper] = binder
            return ServiceToken(contextWrapper)
        }
        return null
    }

    @JvmStatic
    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }
        val mContextWrapper = token.mWrappedContext
        val mBinder = mConnectionMap.remove(mContextWrapper) ?: return
        mContextWrapper.unbindService(mBinder)
        if (mConnectionMap.isEmpty()) {
            musicService = null
        }
    }

    @JvmStatic
    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            println(e.message)
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getQueueDurationSongs(): Int {
        return musicService?.playingQueue?.size ?: -1
    }

    /**
     * Async
     */
    @JvmStatic
    fun playSongAt(position: Int) {
        musicService?.playSongAt(position)
    }

    @JvmStatic
    fun pauseSong() {
        musicService?.pause()
    }

    /**
     * Async
     */
    @JvmStatic
    fun playNextSong() {
        musicService?.nextSong(true)
    }

    /**
     * Async
     */
    @JvmStatic
    fun playPreviousSong() {
        musicService?.previousSong(true)
    }

    /**
     * Async
     */
    @JvmStatic
    fun back() {
        musicService?.back(true)
    }

    @JvmStatic
    fun resumePlaying() {
        musicService?.play()
    }

    /**
     * Async
     */
    @JvmStatic
    fun openQueue(queue: List<Song>, startPosition: Int, startPlaying: Boolean) {
        if (!tryToHandleOpenPlayingQueue(
                queue,
                startPosition,
                startPlaying
            ) && musicService != null
        ) {
            musicService?.openQueue(queue, startPosition, startPlaying)
            if (PreferenceUtil.isShuffleModeOn)
                setShuffleMode(MusicService.SHUFFLE_MODE_NONE)
        }
    }

    /**
     * Async
     */
    @JvmStatic
    fun openAndShuffleQueue(queue: List<Song>, startPlaying: Boolean) {
        var startPosition = 0
        if (queue.isNotEmpty()) {
            startPosition = Random().nextInt(queue.size)
        }

        if (!tryToHandleOpenPlayingQueue(
                queue,
                startPosition,
                startPlaying
            ) && musicService != null
        ) {
            openQueue(queue, startPosition, startPlaying)
            setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE)
        }
    }

    @JvmStatic
    private fun tryToHandleOpenPlayingQueue(
        queue: List<Song>,
        startPosition: Int,
        startPlaying: Boolean
    ): Boolean {
        if (playingQueue === queue) {
            if (startPlaying) {
                playSongAt(startPosition)
            } else {
                position = startPosition
            }
            return true
        }
        return false
    }

    @JvmStatic
    fun getQueueDurationMillis(position: Int): Long {
        return if (musicService != null) {
            musicService!!.getQueueDurationMillis(position)
        } else -1
    }

    @JvmStatic
    fun seekTo(millis: Int): Int {
        return if (musicService != null) {
            musicService!!.seek(millis)
        } else -1
    }

    @JvmStatic
    fun cycleRepeatMode(): Boolean {
        if (musicService != null) {
            musicService?.cycleRepeatMode()
            return true
        }
        return false
    }

    @JvmStatic
    fun toggleShuffleMode(): Boolean {
        if (musicService != null) {
            musicService?.toggleShuffle()
            return true
        }
        return false
    }

    @JvmStatic
    fun setShuffleMode(shuffleMode: Int): Boolean {
        if (musicService != null) {
            musicService!!.shuffleMode = shuffleMode
            return true
        }
        return false
    }

    @JvmStatic
    fun playNext(song: Song): Boolean {
        if (musicService != null) {
            if (playingQueue.size > 0) {
                musicService?.addSong(position + 1, song)
            } else {
                val queue = ArrayList<Song>()
                queue.add(song)
                openQueue(queue, 0, false)
            }
            Toast.makeText(
                musicService,
                musicService!!.resources.getString(R.string.added_title_to_playing_queue),
                Toast.LENGTH_SHORT
            ).show()
            return true
        }
        return false
    }

    @JvmStatic
    fun playNext(songs: List<Song>): Boolean {
        if (musicService != null) {
            if (playingQueue.size > 0) {
                musicService?.addSongs(position + 1, songs)
            } else {
                openQueue(songs, 0, false)
            }
            val toast =
                if (songs.size == 1) musicService!!.resources.getString(R.string.added_title_to_playing_queue) else musicService!!.resources.getString(
                    R.string.added_x_titles_to_playing_queue,
                    songs.size
                )
            Toast.makeText(musicService, toast, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    @JvmStatic
    fun enqueue(song: Song): Boolean {
        if (musicService != null) {
            if (playingQueue.size > 0) {
                musicService?.addSong(song)
            } else {
                val queue = ArrayList<Song>()
                queue.add(song)
                openQueue(queue, 0, false)
            }
            Toast.makeText(
                musicService,
                musicService!!.resources.getString(R.string.added_title_to_playing_queue),
                Toast.LENGTH_SHORT
            ).show()
            return true
        }
        return false
    }

    @JvmStatic
    fun enqueue(songs: List<Song>): Boolean {
        if (musicService != null) {
            if (playingQueue.size > 0) {
                musicService?.addSongs(songs)
            } else {
                openQueue(songs, 0, false)
            }
            val toast =
                if (songs.size == 1) musicService!!.resources.getString(R.string.added_title_to_playing_queue) else musicService!!.resources.getString(
                    R.string.added_x_titles_to_playing_queue,
                    songs.size
                )
            Toast.makeText(musicService, toast, Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    @JvmStatic
    fun removeFromQueue(song: Song): Boolean {
        if (musicService != null) {
            musicService!!.removeSong(song)
            return true
        }
        return false
    }

    @JvmStatic
    fun removeFromQueue(position: Int): Boolean {
        if (musicService != null && position >= 0 && position < playingQueue.size) {
            musicService!!.removeSong(position)
            return true
        }
        return false
    }

    @JvmStatic
    fun moveSong(from: Int, to: Int): Boolean {
        if (musicService != null && from >= 0 && to >= 0 && from < playingQueue.size && to < playingQueue.size) {
            musicService!!.moveSong(from, to)
            return true
        }
        return false
    }

    @JvmStatic
    fun clearQueue(): Boolean {
        if (musicService != null) {
            musicService!!.clearQueue()
            return true
        }
        return false
    }

    @JvmStatic
    fun playFromUri(uri: Uri) {
        if (musicService != null) {

            var songs: List<Song>? = null
            if (uri.scheme != null && uri.authority != null) {
                if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                    var songId: String? = null
                    if (uri.authority == "com.android.providers.media.documents") {
                        songId = getSongIdFromMediaProvider(uri)
                    } else if (uri.authority == "media") {
                        songId = uri.lastPathSegment
                    }
                    if (songId != null) {
                        songs = songRepository.songs(songId)
                    }
                }
            }
            if (songs == null) {
                var songFile: File? = null
                if (uri.authority != null && uri.authority == "com.android.externalstorage.documents") {
                    songFile = File(
                        Environment.getExternalStorageDirectory(),
                        uri.path?.split(":".toRegex(), 2)?.get(1)
                    )
                }
                if (songFile == null) {
                    val path = getFilePathFromUri(musicService!!, uri)
                    if (path != null)
                        songFile = File(path)
                }
                if (songFile == null && uri.path != null) {
                    songFile = File(uri.path)
                }
                if (songFile != null) {
                    songs = songRepository.songsByFilePath(songFile.absolutePath)
                }
            }
            if (songs != null && songs.isNotEmpty()) {
                openQueue(songs, 0, true)
            } else {
                //TODO the file is not listed in the media store
                println("The file is not listed in the media store")
            }
        }
    }

    @JvmStatic
    private fun getSongIdFromMediaProvider(uri: Uri): String {
        return DocumentsContract.getDocumentId(uri).split(":".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()[1]
    }

    class ServiceBinder internal constructor(private val mCallback: ServiceConnection?) :
        ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.service
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
            musicService = null
        }
    }

    class ServiceToken internal constructor(internal var mWrappedContext: ContextWrapper)
}
