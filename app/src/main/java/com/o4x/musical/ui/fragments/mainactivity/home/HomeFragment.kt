package com.o4x.musical.ui.fragments.mainactivity.home

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.o4x.musical.R
import com.o4x.musical.extensions.surfaceColor
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.imageloader.universalil.UniversalIL
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.loader.LastAddedLoader
import com.o4x.musical.loader.SongLoader
import com.o4x.musical.loader.TopAndRecentlyPlayedTracksLoader
import com.o4x.musical.model.smartplaylist.HistoryPlaylist
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.activities.MainActivity.MainActivityFragmentCallbacks
import com.o4x.musical.ui.activities.SearchActivity
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import com.o4x.musical.ui.adapter.home.HomeAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.util.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class HomeFragment : AbsMainActivityFragment(), MainActivityFragmentCallbacks {
    
    private lateinit var activity: MainActivity

    private lateinit var queueAdapter: HomeAdapter
    private lateinit var queueLayoutManager: LinearLayoutManager
    private lateinit var queueListener: QueueListener
    private lateinit var recentlyAdapter: HomeAdapter
    private lateinit var newAdapter: HomeAdapter
    private lateinit var recentlyLayoutManager: GridLayoutManager
    private lateinit var newLayoutManager: GridLayoutManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = mainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onDestroyView() {
        activity.removeMusicServiceEventListener(queueListener)
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        queueListener = QueueListener()
        activity.addMusicServiceEventListener(queueListener)
        activity.setStatusBarColor(transparentColor())
        activity.setNavigationBarColorAuto()
        activity.setTaskDescriptionColorAuto()
        setUpToolbar()
        setUpViews()
    }

    private fun transparentColor(): Int {
        return Color.TRANSPARENT
    }

    private fun setUpToolbar() {
        val transparentColor = transparentColor()
        activity.appbar.setBackgroundColor(transparentColor)
        activity.toolbar.setBackgroundColor(transparentColor)
        activity.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        activity.setTitle(R.string.app_name)
        activity.setSupportActionBar(activity.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(activity,
            activity.toolbar,
            menu,
            ViewUtil.getViewBackgroundColor(activity.toolbar))
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
                startActivity(Intent(activity, SearchActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleBackPress(): Boolean {
        return false
    }

    private fun setAppbarColor(color: Int) {
        val colorFrom = ViewUtil.getViewBackgroundColor(activity.toolbar)
        if (colorFrom == color) return
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom,
            color)
        colorAnimation.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong() // milliseconds
        colorAnimation.addUpdateListener { animator: ValueAnimator ->
            val background = animator.animatedValue as Int
            activity.appbar.setBackgroundColor(background)
            activity.toolbar.setBackgroundColor(background)
        }
        colorAnimation.start()
    }

    private fun setUpViews() {
        setUpHeights()
        setUpOnClicks()
        setUpBounceScrollView()
        setUpQueueView()
        setUpRecentlyView()
        setUpNewView()
        checkIsEmpty()
    }

    private fun setUpHeights() {
        val displayHeight = Resources.getSystem().displayMetrics.heightPixels
        var params: ViewGroup.LayoutParams

        // Set up header height
        params = header.layoutParams
        params.height = displayHeight / 3
        header.layoutParams = params

        // Set up poster image height
        params = poster.layoutParams
        params.height = (displayHeight / 1.5f).toInt()
        poster.layoutParams = params

        // Set up posterGradient height
        poster_gradient.layoutParams = params

        // Set up posterGradient gradient
        //create a new gradient color
        val colors = intArrayOf(
            Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
            PhonographColorUtil.getWindowColor(activity))
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, colors)
        poster_gradient.background = gd
    }

    private fun setUpOnClicks() {
        queue_parent.setOnClickListener { activity.setMusicChooser(R.id.nav_queue) }
        open_queue_button.setOnClickListener { activity.setMusicChooser(R.id.nav_queue) }
        recently_parent.setOnClickListener {
            NavigationUtil.goToPlaylist(
                activity, HistoryPlaylist(activity))
        }
        newly_parent.setOnClickListener {
            NavigationUtil.goToPlaylist(
                activity, LastAddedPlaylist(activity))
        }
        shuffle_btn.setOnClickListener {
            MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(
                activity), true)
        }
    }

    private fun setUpBounceScrollView() {
        val displayHeight = Resources.getSystem().displayMetrics.heightPixels
        val statusBarHeight = Util.getStatusBarHeight(activity)
        val appbarHeight = activity.toolbar.layoutParams.height


        // get real header height
        val headerHeight = header.layoutParams.height - appbarHeight - statusBarHeight.toFloat()
        val transparentColor = transparentColor()
        val isStatusFlat = AtomicBoolean(false)
        val isAppbarFlat = AtomicBoolean(false)
        nested_scroll_view.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->

            // Scroll poster
            poster_parent.y =
                ((-scrollY / (displayHeight * 2 / poster.layoutParams.height.toFloat())).toInt())
                    .toFloat()

            // Scroll appbar
            if (scrollY > headerHeight + appbarHeight && !isAppbarFlat.get()) {
                setAppbarColor(surfaceColor())
                activity.appbar.elevation = 8f
                isAppbarFlat.set(true)
            }
            if (scrollY > headerHeight) {
                if (!isStatusFlat.get()) {
                    activity.setStatusBarColorAutoWithAnim()
                    isStatusFlat.set(true)
                }
                if (scrollY > oldScrollY) {
                    activity.appbar.y =
                        max(-appbarHeight.toFloat(), activity.appbar.y + (oldScrollY - scrollY)
                        )
                    shuffle_btn.hide()
                } else {
                    activity.appbar.y = min(0f, activity.appbar.y + (oldScrollY - scrollY)
                    )
                    shuffle_btn.show()
                }
            } else {
                if (isStatusFlat.get()) {
                    setAppbarColor(transparentColor)
                    activity.setStatusBarColorWithAnim(transparentColor)
                    activity.appbar.elevation = 0f
                    isStatusFlat.set(false)
                    isAppbarFlat.set(false)
                }
                activity.appbar.y = 0f
            }
        }


        // zooming poster in over scroll
        val params = poster.layoutParams
        val width = params.width
        val height = params.height
        nested_scroll_view.setOnOverScrollListener { _: Boolean, overScrolledDistance: Int ->
            val scale = 1 + overScrolledDistance / displayHeight.toFloat()
            val mParams: ViewGroup.LayoutParams =
                FrameLayout.LayoutParams(width, (height * scale).toInt())
            poster.layoutParams = mParams
            poster_parent.layoutParams = mParams
            poster_gradient.layoutParams = mParams
        }
    }

    private fun setUpQueueView() {
        val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
        queueLayoutManager = linearLayoutManager
        queue_recycler_view.layoutManager = queueLayoutManager
        queueAdapter = HomeAdapter(
            activity,
            MusicPlayerRemote.getPlayingQueue(),
            MusicPlayerRemote.getPosition(),
            R.layout.item_card_home,
            null,
            false,
            true)
        queue_recycler_view.adapter = queueAdapter
        queue_recycler_view.itemAnimator = animator
        queueLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition(), 0)
    }

    private fun setUpRecentlyView() {
        recentlyLayoutManager = gridLayoutManager
        recently_recycler_view.layoutManager = recentlyLayoutManager
        recentlyAdapter = HomeAdapter(
            activity,
            TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(activity),
            0,
            R.layout.item_card_home,
            gridSize * 2,
            false,
            false)
        recently_recycler_view.adapter = recentlyAdapter
    }

    private fun setUpNewView() {
        newLayoutManager = gridLayoutManager
        new_recycler_view.layoutManager = newLayoutManager
        newAdapter = HomeAdapter(
            activity,
            LastAddedLoader.getLastAddedSongs(activity),
            0,
            R.layout.item_card_home,
            gridSize * 3,
            false,
            false)
        new_recycler_view.adapter = newAdapter
    }

    private val gridLayoutManager: GridLayoutManager
        get() {
            val size = gridSize
            return object : GridLayoutManager(activity, size) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                    lp.width = width / size
                    lp.height = (lp.width * 1.5).toInt()
                    return super.checkLayoutParams(lp)
                }
            }
        }
    private val linearLayoutManager: LinearLayoutManager
        get() {
            val size = gridSize
            return object : LinearLayoutManager(activity, HORIZONTAL, false) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                    lp.width = width / size
                    lp.height = (lp.width * 1.5).toInt()
                    return super.checkLayoutParams(lp)
                }
            }
        }
    private val gridSize: Int
        get() = resources.getInteger(R.integer.home_grid_columns)

    internal inner class QueueListener : MusicServiceEventListener {
        override fun onServiceConnected() {
            updatePoster()
            updateQueue()
            checkIsEmpty()
        }

        override fun onServiceDisconnected() {}
        override fun onQueueChanged() {
            updateQueue()
        }

        override fun onPlayingMetaChanged() {
            updateQueue()
        }

        override fun onPlayStateChanged() {
            updateQueue()
        }

        override fun onRepeatModeChanged() {}
        override fun onShuffleModeChanged() {}
        override fun onMediaStoreChanged() {
            updateQueue()
        }

        private fun updatePoster() {
            if (MusicPlayerRemote.getPlayingQueue().isNotEmpty()) {
                val song = MusicPlayerRemote.getCurrentSong()
                //                if (navigationDrawerHeader == null) {
//                    navigationDrawerHeader = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
//                    //noinspection ConstantConditions
//                    navigationDrawerHeader.setOnClickListener(v -> {
//                        drawerLayout.closeDrawers();
//                        if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
//                            expandPanel();
//                        }
//                    });
//                }
//                ((TextView) navigationDrawerHeader.findViewById(R.id.title)).setText(song.title);
//                ((TextView) navigationDrawerHeader.findViewById(R.id.text)).setText(MusicUtil.getSongInfoString(song));
                UniversalIL.getImageLoader().displayImage(
                    MusicUtil.getMediaStoreAlbumCoverUri(song.albumId).toString(),
                    poster
                )
            } else {
//                if (navigationDrawerHeader != null) {
//                    navigationView.removeHeaderView(navigationDrawerHeader);
//                    navigationDrawerHeader = null;
//                }
            }
        }

        private fun updateQueue() {
            queueAdapter.swapDataSet(MusicPlayerRemote.getPlayingQueue(),
                MusicPlayerRemote.getPosition())
            resetToCurrentPosition()
        }

        private fun resetToCurrentPosition() {
            if (queueAdapter.itemCount == 0) return
            queue_recycler_view.stopScroll()
            val from = queueLayoutManager.findFirstVisibleItemPosition()
            val to = MusicPlayerRemote.getPosition()
            val delta = abs(to - from)
            val limit = 150
            if (delta > limit) {
                queueLayoutManager.scrollToPosition(
                    to + if (to > from) -limit else limit
                )
            }
            val smoothScroller: SmoothScroller = object : LinearSmoothScroller(activity) {
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

        init {
            updatePoster()
        }
    }

    private fun checkIsEmpty() {
        if (empty != null) {
            empty.visibility = if ((queueAdapter.itemCount == 0) and
                (recentlyAdapter.itemCount == 0) and
                (newAdapter.itemCount == 0)
            ) View.VISIBLE else View.GONE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}