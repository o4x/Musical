package github.o4x.m2.util

import android.media.MediaScannerConnection
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import github.o4x.m2.App
import java.util.concurrent.atomic.AtomicInteger


fun scanPaths(
    toBeScanned: Array<String>?
) {
    val context = App.getContext()

    if (toBeScanned == null || toBeScanned.isEmpty()) {
        return
    }

    val remaining = AtomicInteger(toBeScanned.size)
    MediaScannerConnection.scanFile(
        context,
        toBeScanned,
        null
    ) { _, _ ->
        // onScanCompleted runs on a background HandlerThread.
        if (remaining.decrementAndGet() == 0) {
            // Disk cache must be cleared off the main thread; memory cache on it.
            // Both must be invalidated so stale album art is never served from cache,
            // even when the file's dateModified didn't change (e.g. write failed but
            // insertAlbumArt/deleteAlbumArt updated the MediaStore albumart table).
            Glide.get(context).clearDiskCache()
            Handler(Looper.getMainLooper()).post {
                Glide.get(context).clearMemory()
            }
        }
    }
}
