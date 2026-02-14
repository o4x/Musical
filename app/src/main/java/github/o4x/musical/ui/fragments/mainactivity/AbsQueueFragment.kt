package github.o4x.musical.ui.fragments.mainactivity

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import github.o4x.musical.helper.MusicPlayerRemote
import github.o4x.musical.ui.adapter.song.PlayingQueueAdapter
import kotlin.math.abs

abstract class AbsQueueFragment(@LayoutRes layout: Int) : AbsMainActivityFragment(layout) {

    protected abstract val queueRecyclerView: RecyclerView

    lateinit var queueAdapter: PlayingQueueAdapter
    lateinit var queueLayoutManager: LinearLayoutManager

    var isRestored = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initQueueView()

        playerViewModel.queue.observe(viewLifecycleOwner, {
            queueAdapter.swapDataSet(it, MusicPlayerRemote.position)
            isRestored = false
        })
        playerViewModel.position.observe(viewLifecycleOwner, {
            queueAdapter.setCurrent(it)
            toPosition(it)
        })
        playerViewModel.isPlaying.observe(viewLifecycleOwner, {
            queueAdapter.notifyDataSetChanged()
        })
    }

    override fun onStop() {
        super.onStop()
        isRestored = false
    }

    abstract fun initQueueView()

    private fun toPosition(position: Int) {
        queueRecyclerView.stopScroll()
        if (isRestored) {
            resetToPosition(position)
        } else {
            queueLayoutManager.scrollToPosition(position)
            isRestored = true
        }
    }

    private fun resetToPosition(to: Int) {
        if (queueAdapter.itemCount == 0) return
        val from = queueLayoutManager.findFirstVisibleItemPosition()
        val delta = abs(to - from)

        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(serviceActivity) {
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
}
