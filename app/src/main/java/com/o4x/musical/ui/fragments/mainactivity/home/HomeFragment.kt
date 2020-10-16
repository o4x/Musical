package com.o4x.musical.ui.fragments.mainactivity.home

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.ColorUtil.isColorLight
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getPrimaryTextColor
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getSecondaryTextColor
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.o4x.musical.R
import com.o4x.musical.extensions.isDarkMode
import com.o4x.musical.extensions.surfaceColor
import com.o4x.musical.extensions.themeColor
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.imageloader.universalil.listener.PaletteMusicLoadingListener
import com.o4x.musical.imageloader.universalil.loader.UniversalIL
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.loader.LastAddedLoader
import com.o4x.musical.loader.TopAndRecentlyPlayedTracksLoader
import com.o4x.musical.model.smartplaylist.HistoryPlaylist
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist
import com.o4x.musical.ui.adapter.home.HomeAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.util.CoverUtil
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.ViewUtil
import com.o4x.musical.util.color.MediaNotificationProcessor
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class HomeFragment : AbsMainActivityFragment(R.layout.fragment_home) {

    private lateinit var queueAdapter: HomeAdapter
    private lateinit var queueLayoutManager: LinearLayoutManager
    private lateinit var queueListener: QueueListener
    private lateinit var recentlyAdapter: HomeAdapter
    private lateinit var newAdapter: HomeAdapter
    private lateinit var recentlyLayoutManager: GridLayoutManager
    private lateinit var newLayoutManager: GridLayoutManager

    // Heights //
    private var displayHeight by Delegates.notNull<Int>()
    private var appbarHeight by Delegates.notNull<Int>()
    private var toolbarHeight by Delegates.notNull<Int>()
    private var headerHeight by Delegates.notNull<Int>()

    // animations //
    private val toolbarAnimation = ValueAnimator.ofFloat(0f, 1f)
    private val statusAnimation = ValueAnimator.ofFloat(0f, 1f)

    var position: Int = 0

    override fun onDestroyView() {
        mainActivity.removeMusicServiceEventListener(queueListener)
        toolbarAnimation.cancel()
        statusAnimation.cancel()
        super.onDestroyView()
    }

    override fun onPause() {
        // Save scroll view position
        position = nested_scroll_view.scrollY

        mainActivity.removeMusicServiceEventListener(queueListener)

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        showAppbar()
        if (position < headerHeight) {
            toolbarColorVisible(false)
            statusBarColorVisible(false)
            mainActivity.appbar.elevation = 0f
        }

        mainActivity.addMusicServiceEventListener(queueListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        queueListener = QueueListener()
        hideSubToolbar()
        setUpViews()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            mainActivity,
            mainActivity.toolbar,
            menu,
            surfaceColor()
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(activity, mainActivity.toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shuffle_all -> {
                libraryViewModel.shuffleSongs()
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
        setUpHeights()
        setupButtons()
        setUpBounceScrollView()
        setUpQueueView()
        setUpRecentlyView()
        setUpNewView()
        checkIsEmpty()
    }

    private fun setUpHeights() {
        displayHeight = Resources.getSystem().displayMetrics.heightPixels
        var params: ViewGroup.LayoutParams

        // Set up header height
        params = header.layoutParams
        params.height = displayHeight / 3
        header.layoutParams = params

        // Set up poster image height
        params = poster.layoutParams
        params.height = (displayHeight / 1.5f).toInt()
        poster.layoutParams = params


        appbarHeight = appbarHeight()
        toolbarHeight = toolbarHeight()
        // get real header height
        headerHeight = header.layoutParams.height - appbarHeight
    }

    private fun setupButtons() {
        queue_parent.setOnClickListener { mainActivity.setMusicChooser(R.id.nav_queue) }
        open_queue_button.setOnClickListener { mainActivity.setMusicChooser(R.id.nav_queue) }
        recently_parent.setOnClickListener {
            NavigationUtil.goToPlaylist(
                mainActivity, HistoryPlaylist()
            )
        }
        newly_parent.setOnClickListener {
            NavigationUtil.goToPlaylist(
                mainActivity, LastAddedPlaylist()
            )
        }
        shuffle_btn.setOnClickListener {
            libraryViewModel.shuffleSongs()
        }
        shuffle_btn.backgroundTintList = ColorStateList.valueOf(themeColor())
        shuffle_btn.setColorFilter(
            getSecondaryTextColor(activity, isColorLight(themeColor())),
            PorterDuff.Mode.SRC_IN
        )
        open_queue_button.setBackgroundColor(themeColor())
        open_queue_button.setTextColor(
            getPrimaryTextColor(activity, isColorLight(themeColor()))
        )
    }

    private fun setUpBounceScrollView() {

        var isStatusFlat = false
        var isAppbarFlat = false

        nested_scroll_view.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->

            // Scroll poster
            poster.y =
                ((-scrollY / (displayHeight * 2 / poster.layoutParams.height.toFloat())).toInt())
                    .toFloat()

            // Scroll appbar
            if (scrollY > headerHeight + toolbarHeight && !isAppbarFlat) {
                toolbarColorVisible(true)
                mainActivity.appbar.elevation = resources.getDimension(R.dimen.appbar_elevation)
                isAppbarFlat = true
            }
            if (scrollY > headerHeight) { // outside header
                if (!isStatusFlat) {
                    statusBarColorVisible(true)
                    isStatusFlat = true
                }
                if (oldScrollY != 0) {
                    if (scrollY > oldScrollY) { // Scrolling up
                        mainActivity.appbar.y =
                            max(
                                -toolbarHeight.toFloat(),
                                mainActivity.appbar.y + (oldScrollY - scrollY)
                            )
                        shuffle_btn.hide()
                    } else { // Scrolling down
                        mainActivity.appbar.y = min(
                            0f, mainActivity.appbar.y + (oldScrollY - scrollY)
                        )
                        shuffle_btn.show()
                    }
                }
            } else { // inside header
                if (isStatusFlat) {
                    toolbarColorVisible(false)
                    statusBarColorVisible(false)
                    mainActivity.appbar.elevation = 0f
                    isStatusFlat = false
                    isAppbarFlat = false
                }
                mainActivity.appbar.y = 0f
            }
        }


        // zooming poster in over scroll
        nested_scroll_view.setOnOverScrollListener { _: Boolean, overScrolledDistance: Int ->
            val scale = 1 + overScrolledDistance / displayHeight.toFloat()
            poster.scaleX = scale
            poster.scaleY = scale
        }
    }

    private fun setUpQueueView() {
        val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
        queueLayoutManager = linearLayoutManager
        queue_recycler_view.layoutManager = queueLayoutManager
        queueAdapter = HomeAdapter(
            mainActivity,
            MusicPlayerRemote.playingQueue,
            MusicPlayerRemote.position,
            R.layout.item_card_home,
            null,
            true,
            true
        )
        queue_recycler_view.adapter = queueAdapter
        queue_recycler_view.itemAnimator = animator
        queueLayoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position, 0)
    }

    private fun setUpRecentlyView() {
        recentlyLayoutManager = gridLayoutManager
        recently_recycler_view.layoutManager = recentlyLayoutManager
        recentlyAdapter = HomeAdapter(
            mainActivity,
            TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mainActivity),
            0,
            R.layout.item_card_home,
            gridSize * 2,
            true,
            false
        )
        recently_recycler_view.adapter = recentlyAdapter
    }

    private fun setUpNewView() {
        newLayoutManager = gridLayoutManager
        new_recycler_view.layoutManager = newLayoutManager
        newAdapter = HomeAdapter(
            mainActivity,
            LastAddedLoader.getLastAddedSongs(mainActivity),
            0,
            R.layout.item_card_home,
            gridSize * 3,
            true,
            false
        )
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
            if (MusicPlayerRemote.playingQueue.isNotEmpty()) {
                val song = MusicPlayerRemote.currentSong

                UniversalIL().withListener(
                    object : PaletteMusicLoadingListener() {

                        var bitmap: Bitmap? = null

                        override fun onLoadingComplete(
                            imageUri: String,
                            view: View?,
                            loadedImage: Bitmap
                        ) {
                            super.onLoadingComplete(imageUri, view, loadedImage)
                            bitmap = loadedImage
                        }

                        override fun onColorReady(colors: MediaNotificationProcessor) {
                            poster.background =
                                BitmapDrawable(
                                    resources,
                                    CoverUtil.doubleGradient(
                                        colors.backgroundColor,
                                        colors.mightyColor
                                    )
                                )

                            bitmap?.let {
                                if (requireContext().isDarkMode ==
                                    !ColorUtil.isColorLight(colors.backgroundColor)
                                ) {
                                    poster.setPadding(0)
                                    poster.setImageBitmap(CoverUtil.addGradientTo(it))
                                } else {
                                    poster.setPadding(appbarHeight)
                                    poster.setImageBitmap(it)
                                }
                            }
                        }

                    }
                ).byThis(song).loadImage(requireContext())

            }
        }

        private fun updateQueue() {
            queueAdapter.swapDataSet(
                MusicPlayerRemote.playingQueue,
                MusicPlayerRemote.position
            )
            resetToCurrentPosition()
        }

        private fun resetToCurrentPosition() {
            if (queueAdapter.itemCount == 0) return
            queue_recycler_view.stopScroll()
            val from = queueLayoutManager.findFirstVisibleItemPosition()
            val to = MusicPlayerRemote.position
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

    private fun toolbarColorVisible(show: Boolean) {
        toolbarAnimation.cancel()

        val color = surfaceColor()
        val current = ViewUtil.getViewBackgroundColor(mainActivity.toolbar)

        // break if current color equal final color
        if (
            show && current == ColorUtil.withAlpha(current, 1f)
            ||
            !show && current == ColorUtil.withAlpha(current, 0f)
        ) {
            mainActivity.toolbar.setBackgroundColor(
                if (show) color else ColorUtil.withAlpha(
                    color,
                    0f
                )
            )
            return
        }

        toolbarAnimation.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong() // milliseconds
        toolbarAnimation.addUpdateListener { animator: ValueAnimator ->
            val f = if (show) animator.animatedFraction else 1 - animator.animatedFraction
            mainActivity.toolbar.setBackgroundColor(ColorUtil.withAlpha(color, f))
        }
        toolbarAnimation.start()
    }

    private fun statusBarColorVisible(show: Boolean) {
        statusAnimation.cancel()

        val color = surfaceColor()
        val current = mainActivity.window.statusBarColor

        // break if current color equal final color
        if (
            show && current == ColorUtil.withAlpha(current, 1f)
            ||
            !show && current == ColorUtil.withAlpha(current, 0f)
        ) {
            mainActivity.window.statusBarColor = if (show) color else ColorUtil.withAlpha(color, 0f)
            return
        }

        statusAnimation.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong() // milliseconds
        statusAnimation.addUpdateListener { animator: ValueAnimator ->
            val f = if (show) animator.animatedFraction else 1 - animator.animatedFraction
            mainActivity.window.statusBarColor = ColorUtil.withAlpha(color, f)
        }
        mainActivity.setLightStatusBarAuto(color)
        statusAnimation.start()
    }
}