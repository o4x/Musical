package com.o4x.musical.ui.fragments.mainactivity

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.ui.adapter.song.PlayingQueueAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.math.abs

open class AbsQueueFragment(@LayoutRes layout: Int) : AbsMainActivityFragment(layout), MusicServiceEventListener {

    lateinit var queueAdapter: PlayingQueueAdapter
    lateinit var queueLayoutManager: LinearLayoutManager

    override fun onResume() {
        super.onResume()
        mainActivity.addMusicServiceEventListener(this)
        toCurrentPosition()
    }

    override fun onDestroyView() {
        mainActivity.removeMusicServiceEventListener(this)
        super.onDestroyView()
    }

    override fun onPause() {
        mainActivity.removeMusicServiceEventListener(this)
        super.onPause()
    }

    fun toCurrentPosition() {
        queueLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position, 0)
    }

    private fun resetToCurrentPosition() {
        if (queueAdapter.itemCount == 0) return
        queue_recycler_view.stopScroll()
        val from = queueLayoutManager.findFirstVisibleItemPosition()
        val to = MusicPlayerRemote.position
        val delta = abs(to - from)

        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(activity) {
            override fun getHorizontalSnapPreference(): Int {
                return SNAP_TO_ANY
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return if (delta < 20) {
                    super.calculateSpeedPerPixel(displayMetrics) * 5
                } else {
                    super.calculateSpeedPerPixel(displayMetrics)
                }
            }
        }

        smoothScroller.targetPosition = to
        queueLayoutManager.startSmoothScroll(smoothScroller)
    }

    override fun onServiceConnected() {
        resetToCurrentPosition()
    }

    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}

    override fun onPlayingMetaChanged() {
        resetToCurrentPosition()
    }

    override fun onPlayStateChanged() {}

    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
    override fun onMediaStoreChanged() {}
}