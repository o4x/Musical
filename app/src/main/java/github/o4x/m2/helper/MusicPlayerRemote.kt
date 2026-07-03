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

package github.o4x.m2.helper

import android.app.Activity
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import github.o4x.m2.R
import github.o4x.m2.model.Song
import github.o4x.m2.repository.SongRepository
import github.o4x.m2.service.MusicService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

object MusicPlayerRemote : KoinComponent {
    val TAG: String = MusicPlayerRemote::class.java.simpleName

    private val mConnectionMap = WeakHashMap<Context, ServiceBinder>()

    private var musicServiceRef: WeakReference<MusicService>? = null

    // The backing reference is weak, so read this property once into a local
    // and use the snapshot; a second read may already return null.
    @JvmStatic
    var musicService: MusicService?
        get() = musicServiceRef?.get()
        set(value) { musicServiceRef = value?.let { WeakReference(it) } }

    private val songRepository by inject<SongRepository>()

    @JvmStatic
    fun bindToService(context: Context, callback: ServiceConnection): ServiceToken? {
        val realActivity: Activity = (context as Activity).parent ?: context

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
        val contextWrapper = token.mWrappedContext
        val binder = mConnectionMap.remove(contextWrapper) ?: return
        contextWrapper.unbindService(binder)
        if (mConnectionMap.isEmpty()) {
            musicService = null
        }
    }

    @JvmStatic
    fun notifyMediaStoreChanged() {
        musicService?.notifyChange(MusicService.MEDIA_STORE_CHANGED)
    }

    @JvmStatic
    val isPlaying: Boolean
        get() = musicService?.isPlaying == true

    @JvmStatic
    fun isPlaying(song: Song): Boolean {
        return isPlaying && song.id == currentSong.id
    }

    @JvmStatic
    val currentSong: Song
        get() = musicService?.currentSong ?: Song.emptySong

    /**
     * Async
     */
    @JvmStatic
    var position: Int
        get() = musicService?.position ?: -1
        set(position) {
            musicService?.setPosition(position)
        }

    @JvmStatic
    val playingQueue: List<Song>
        get() = musicService?.playingQueue ?: listOf()

    @JvmStatic
    val songProgressMillis: Int
        get() = musicService?.songProgressMillis ?: -1

    @JvmStatic
    val songDurationMillis: Int
        get() = musicService?.songDurationMillis ?: -1

    @JvmStatic
    val repeatMode: Int
        get() = musicService?.repeatMode ?: MusicService.REPEAT_MODE_NONE

    @JvmStatic
    val shuffleMode: Int
        get() = musicService?.shuffleMode ?: MusicService.SHUFFLE_MODE_NONE

    @JvmStatic
    val audioSessionId: Int
        get() = musicService?.audioSessionId ?: -1

    @JvmStatic
    val isServiceConnected: Boolean
        get() = musicService != null

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
        if (tryToHandleOpenPlayingQueue(queue, startPosition, startPlaying)) return
        val service = musicService ?: return
        if (service.shuffleMode == MusicService.SHUFFLE_MODE_SHUFFLE) {
            service.setShuffleMode(MusicService.SHUFFLE_MODE_NONE)
        }
        service.openQueue(queue, startPosition, startPlaying)
    }

    /**
     * Async
     */
    @JvmStatic
    fun openAndShuffleQueue(queue: List<Song>, startPlaying: Boolean) {
        musicService?.playSongs(queue, MusicService.SHUFFLE_MODE_SHUFFLE, startPlaying)
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
        return musicService?.getQueueDurationMillis(position) ?: -1
    }

    @JvmStatic
    fun seekTo(millis: Int): Int {
        return musicService?.seek(millis) ?: -1
    }

    @JvmStatic
    fun cycleRepeatMode(): Boolean {
        val service = musicService ?: return false
        service.cycleRepeatMode()
        return true
    }

    @JvmStatic
    fun toggleShuffleMode(): Boolean {
        val service = musicService ?: return false
        service.toggleShuffle()
        return true
    }

    @JvmStatic
    fun setShuffleMode(shuffleMode: Int): Boolean {
        val service = musicService ?: return false
        service.setShuffleMode(shuffleMode)
        return true
    }

    @JvmStatic
    fun playNext(song: Song): Boolean {
        val service = musicService ?: return false
        if (service.playingQueue.isNotEmpty()) {
            service.addSong(service.position + 1, song)
        } else {
            openQueue(listOf(song), 0, false)
        }
        showAddedToQueueToast(service, 1)
        return true
    }

    @JvmStatic
    fun playNext(songs: List<Song>): Boolean {
        val service = musicService ?: return false
        if (service.playingQueue.isNotEmpty()) {
            service.addSongs(service.position + 1, songs)
        } else {
            openQueue(songs, 0, false)
        }
        showAddedToQueueToast(service, songs.size)
        return true
    }

    @JvmStatic
    fun enqueue(song: Song): Boolean {
        val service = musicService ?: return false
        if (service.playingQueue.isNotEmpty()) {
            service.addSong(song)
        } else {
            openQueue(listOf(song), 0, false)
        }
        showAddedToQueueToast(service, 1)
        return true
    }

    @JvmStatic
    fun enqueue(songs: List<Song>): Boolean {
        val service = musicService ?: return false
        if (service.playingQueue.isNotEmpty()) {
            service.addSongs(songs)
        } else {
            openQueue(songs, 0, false)
        }
        showAddedToQueueToast(service, songs.size)
        return true
    }

    private fun showAddedToQueueToast(service: MusicService, songCount: Int) {
        val toast = if (songCount == 1) {
            service.resources.getString(R.string.added_title_to_playing_queue)
        } else {
            service.resources.getString(R.string.added_x_titles_to_playing_queue, songCount)
        }
        Toast.makeText(service, toast, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun removeFromQueue(song: Song): Boolean {
        val service = musicService ?: return false
        service.removeSong(song)
        return true
    }

    @JvmStatic
    fun removeFromQueue(position: Int): Boolean {
        val service = musicService ?: return false
        if (position !in service.playingQueue.indices) return false
        service.removeSong(position)
        return true
    }

    @JvmStatic
    fun moveSong(from: Int, to: Int): Boolean {
        val service = musicService ?: return false
        val indices = service.playingQueue.indices
        if (from !in indices || to !in indices) return false
        service.moveSong(from, to)
        return true
    }

    @JvmStatic
    fun clearQueue(): Boolean {
        val service = musicService ?: return false
        service.clearQueue()
        return true
    }

    @JvmStatic
    fun playFromUri(uri: Uri) {
        val service = musicService ?: return

        var songs: List<Song>? = null
        if (uri.scheme != null && uri.authority != null) {
            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val songId = when (uri.authority) {
                    "com.android.providers.media.documents" -> getSongIdFromMediaProvider(uri)
                    "media" -> uri.lastPathSegment
                    else -> null
                }
                if (songId != null) {
                    songs = songRepository.songs(songId)
                }
            }
        }
        if (songs == null) {
            var songFile: File? = null
            if (uri.authority == "com.android.externalstorage.documents") {
                songFile = File(
                    Environment.getExternalStorageDirectory(),
                    uri.path?.split(":".toRegex(), 2)?.get(1)
                )
            }
            if (songFile == null) {
                val path = getFilePathFromUri(service, uri)
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
        if (!songs.isNullOrEmpty()) {
            openQueue(songs, 0, true)
        } else {
            //TODO the file is not listed in the media store
            println("The file is not listed in the media store")
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
