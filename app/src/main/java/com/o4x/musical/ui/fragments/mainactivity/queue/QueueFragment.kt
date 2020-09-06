package com.o4x.musical.ui.fragments.mainactivity.queue

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import code.name.monkey.appthemehelper.ThemeStore.Companion.themeColor
import code.name.monkey.appthemehelper.common.ATHToolbarActivity
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.loader.SongLoader
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.activities.MainActivity.MainActivityFragmentCallbacks
import com.o4x.musical.ui.activities.SearchActivity
import com.o4x.musical.ui.adapter.song.PlayingQueueAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import kotlinx.android.synthetic.main.fragment_queue.*

class QueueFragment : AbsMainActivityFragment(), MainActivityFragmentCallbacks {
    
    private lateinit var activity: MainActivity
    
    private var queueAdapter: PlayingQueueAdapter? = null
    private var queueLayoutManager: LinearLayoutManager? = null
    private var queueListener: QueueListener? = null
    private var wrappedAdapter: RecyclerView.Adapter<*>? = null
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = mainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_queue, container, false)
        return view
    }

    override fun onDestroyView() {
        activity.removeMusicServiceEventListener(queueListener)
        queueAdapter = null
        queueLayoutManager = null
        queueListener = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        queueListener = QueueListener()
        activity.addMusicServiceEventListener(queueListener)
        mainActivity.setStatusBarColorAuto()
        mainActivity.setNavigationBarColorAuto()
        mainActivity.setTaskDescriptionColorAuto()
        setUpToolbar()
        setUpViews()
    }

    private fun setUpToolbar() {
        val primaryColor = themeColor(activity)
        activity.appbar.setBackgroundColor(primaryColor)
        activity.toolbar.setBackgroundColor(primaryColor)
        activity.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        activity.setTitle(R.string.app_name)
        mainActivity.setSupportActionBar(activity.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(activity,
            activity.toolbar,
            menu,
            ATHToolbarActivity.getToolbarBackgroundColor(activity.toolbar))
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(activity, activity.toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shuffle_all -> {
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(activity), true)
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

    override fun handleBackPress(): Boolean {
        return false
    }

    private fun setUpViews() {
        setUpQueueView()
        checkIsEmpty()
    }

    private fun setUpQueueView() {
        recyclerViewDragDropManager = RecyclerViewDragDropManager()
        val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
        queueAdapter = PlayingQueueAdapter(
            getActivity() as AppCompatActivity?,
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


//        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
//        queueLayoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
//        queueView.setLayoutManager(queueLayoutManager);
//        queueAdapter = new PlayingQueueAdapter(
//                ((AppCompatActivity) getActivity()),
//                MusicPlayerRemote.getPlayingQueue(),
//                MusicPlayerRemote.getPosition(),
//                R.layout.item_list_no_image,
//                false,
//                null
//        );
//        queueView.setAdapter(queueAdapter);
//        queueView.setItemAnimator(animator);
//        queueLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition(), 0);
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