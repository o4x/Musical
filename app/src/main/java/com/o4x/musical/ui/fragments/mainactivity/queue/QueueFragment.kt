package com.o4x.musical.ui.fragments.mainactivity.queue

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import code.name.monkey.appthemehelper.common.ATHToolbarActivity
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.loader.SongLoader
import com.o4x.musical.ui.activities.SearchActivity
import com.o4x.musical.ui.adapter.song.PlayingQueueAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import kotlinx.android.synthetic.main.fragment_queue.*
import kotlin.math.max
import kotlin.math.min

class QueueFragment : AbsMainActivityFragment() {

    private var queueAdapter: PlayingQueueAdapter? = null
    private var queueLayoutManager: LinearLayoutManager? = null
    private var queueListener: QueueListener? = null
    private var wrappedAdapter: RecyclerView.Adapter<*>? = null
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_queue, container, false)
    }

    override fun onDestroyView() {
        mainActivity.removeMusicServiceEventListener(queueListener)
        queueAdapter = null
        queueLayoutManager = null
        queueListener = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        queueListener = QueueListener()
        mainActivity.addMusicServiceEventListener(queueListener)
        setUpViews()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(mainActivity,
            mainActivity.toolbar,
            menu,
            ATHToolbarActivity.getToolbarBackgroundColor(mainActivity.toolbar))
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(activity, mainActivity.toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shuffle_all -> {
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(mainActivity), true)
                return true
            }
            R.id.action_new_playlist -> {
                CreatePlaylistDialog.create().show(childFragmentManager, "CREATE_PLAYLIST")
                return true
            }
            R.id.action_search -> {
                startActivity(Intent(getActivity(), SearchActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpViews() {
        setUpQueueView()
        checkIsEmpty()
    }

    private fun setUpQueueView() {
        recyclerViewDragDropManager = RecyclerViewDragDropManager()
        val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
        queueAdapter = PlayingQueueAdapter(
            mainActivity,
            MusicPlayerRemote.getPlayingQueue(),
            MusicPlayerRemote.getPosition(),
            R.layout.item_list,
            false,
            null)
        wrappedAdapter = recyclerViewDragDropManager!!.createWrappedAdapter(queueAdapter!!)
        queueLayoutManager = LinearLayoutManager(getActivity())
        queue_recycler_view!!.layoutManager = queueLayoutManager
        queue_recycler_view!!.adapter = wrappedAdapter
        queue_recycler_view!!.itemAnimator = animator
        recyclerViewDragDropManager!!.attachRecyclerView(queue_recycler_view!!)
        queueLayoutManager!!.scrollToPositionWithOffset(MusicPlayerRemote.getPosition() + 1, 0)

        setupRecyclerWithAppbar()
    }

    private fun setupRecyclerWithAppbar() {
        val appbarHeight = appbarHeight()
        val toolbarHeight = toolbarHeight()

        queue_recycler_view.setPadding(0, appbarHeight, 0, 0);

        queue_recycler_view.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    when {
                        dy > 0 -> { // Scrolling up
                            val changes = max(-toolbarHeight.toFloat(), mainActivity.appbar.y - (dy))
                            mainActivity.appbar.y = changes
                            queue_recycler_view.setPadding(0, (changes + appbarHeight).toInt(), 0, 0);
                        }
                        dy < 0 -> { // Scrolling down
                            val changes = min(0f, mainActivity.appbar.y - (dy))
                            mainActivity.appbar.y = changes
                            queue_recycler_view.setPadding(0, (changes + appbarHeight).toInt(), 0, 0);
                        }
                        else -> { // on start page
                            showAppbar()
                        }
                    }

                }

            }
        )
    }

    internal inner class QueueListener : MusicServiceEventListener {
        override fun onServiceConnected() {
            updateQueue()
        }

        override fun onServiceDisconnected() {}
        override fun onQueueChanged() {
            updateQueue()
        }

        override fun onPlayingMetaChanged() {
            updateQueue()
        }

        override fun onPlayStateChanged() {}
        override fun onRepeatModeChanged() {}
        override fun onShuffleModeChanged() {}
        override fun onMediaStoreChanged() {
            updateQueue()
        }

        private fun updateQueue() {
            queueAdapter!!.swapDataSet(MusicPlayerRemote.getPlayingQueue(),
                MusicPlayerRemote.getPosition())
            resetToCurrentPosition()
            checkIsEmpty()
        }

        private fun resetToCurrentPosition() {
            if (queueAdapter!!.itemCount == 0) return
            queue_recycler_view!!.stopScroll()
            val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_ANY
                }
            }
            try {
                smoothScroller.targetPosition = MusicPlayerRemote.getPosition() + 2
                queueLayoutManager!!.startSmoothScroll(smoothScroller)
            } catch (e: Exception) {
            }
        }
    }

    private fun checkIsEmpty() {
        if (empty != null) {
            empty!!.visibility =
                if (queueAdapter == null || queueAdapter!!.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    companion object {
        fun newInstance(): QueueFragment {
            return QueueFragment()
        }
    }
}