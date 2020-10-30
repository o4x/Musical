package com.o4x.musical.ui.fragments.mainactivity.queue

import android.os.Bundle
import android.view.*
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
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.repository.RealSongRepository
import com.o4x.musical.ui.adapter.song.PlayingQueueAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.ui.fragments.mainactivity.AbsQueueFragment
import kotlinx.android.synthetic.main.fragment_queue.*

class QueueFragment : AbsQueueFragment(R.layout.fragment_queue) {

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private lateinit var recyclerViewDragDropManager: RecyclerViewDragDropManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                MusicPlayerRemote.openAndShuffleQueue(RealSongRepository(mainActivity).songs(), true)
                return true
            }
            R.id.action_new_playlist -> {
                CreatePlaylistDialog.create().show(childFragmentManager, "CREATE_PLAYLIST")
                return true
            }
            R.id.action_search -> {
                mainActivity.openSearch()
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
            MusicPlayerRemote.playingQueue,
            MusicPlayerRemote.position,
            R.layout.item_list,
            null)
        wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(queueAdapter)
        queueLayoutManager = OverScrollLinearLayoutManager(requireContext())
        queue_recycler_view?.layoutManager = queueLayoutManager
        queue_recycler_view?.adapter = wrappedAdapter
        queue_recycler_view?.itemAnimator = animator
        recyclerViewDragDropManager.attachRecyclerView(queue_recycler_view)

        libraryViewModel.getQueue().observe(viewLifecycleOwner, {
            queueAdapter.swapDataSet(it, MusicPlayerRemote.position)
            checkIsEmpty()
        })

        queue_recycler_view.addAppbarListener()
    }

    private fun checkIsEmpty() {
        if (empty != null) {
            empty!!.visibility =
                if (queueAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }
}