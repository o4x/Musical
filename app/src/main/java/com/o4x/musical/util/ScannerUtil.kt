package com.o4x.musical.util

import android.media.MediaScannerConnection
import com.o4x.musical.App


fun scanPaths(
    toBeScanned: Array<String>?
) {
    val context = App.getContext()

    if (toBeScanned == null || toBeScanned.isEmpty()) {
        return
    } else {
        MediaScannerConnection.scanFile(
            context,
            toBeScanned,
            null,
            null
        )
    }
}