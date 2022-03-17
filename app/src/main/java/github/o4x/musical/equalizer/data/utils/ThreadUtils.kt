@file:Suppress("NOTHING_TO_INLINE")

package github.o4x.musical.equalizer.data.utils

import android.os.Looper
import github.o4x.musical.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect

private val isTestMode by lazy {
    // TODO only in unit tests??
    try {
        Class.forName("org.junit.Test")
        true
    } catch (ignored: Throwable) {
        false
    }
}

inline fun isMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun <T> Flow<T>.assertBackground(): Flow<T> {
    return channelFlow {
        assertBackgroundThread()
        collect { offer(it) }
    }
}

fun assertBackgroundThread() {
    if (!isTestMode && BuildConfig.DEBUG && isMainThread()) {
        throw AssertionError("not on worker thread, current=${Thread.currentThread()}")
    }
}