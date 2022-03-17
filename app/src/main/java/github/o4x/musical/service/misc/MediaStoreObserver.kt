package github.o4x.musical.service.misc

import android.database.ContentObserver
import android.os.Handler
import android.provider.MediaStore
import github.o4x.musical.service.MusicService

class MediaStoreObserver(private val service: MusicService, private val mHandler: Handler) {

    companion object {
        // milliseconds to delay before calling refresh to aggregate events
        private const val REFRESH_DELAY: Long = 500
    }

    private val observer = object : ContentObserver(mHandler), Runnable {
        override fun onChange(selfChange: Boolean) {
            // if a change is detected, remove any scheduled callback
            // then post a new one. This is intended to prevent closely
            // spaced events from generating multiple refresh calls
            mHandler.removeCallbacks(this)
            mHandler.postDelayed(this, REFRESH_DELAY)
        }

        override fun run() {
            // actually call refresh when the delayed callback fires
            // do not send a sticky broadcast here
            service.handleAndSendChangeInternal(MusicService.MEDIA_STORE_CHANGED)
        }
    }

    private val contentResolver = service.contentResolver

    fun start() {
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Albums.INTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Artists.INTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Genres.INTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Playlists.INTERNAL_CONTENT_URI,
            true,
            observer
        )
    }

    fun cancel() {
        contentResolver.unregisterContentObserver(observer)
    }
}