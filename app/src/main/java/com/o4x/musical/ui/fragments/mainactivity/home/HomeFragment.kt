package com.o4x.musical.ui.fragments.mainactivity.home

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.util.ColorUtil.isColorLight
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getPrimaryTextColor
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.o4x.musical.R
import com.o4x.musical.extensions.themeColor
import com.o4x.musical.extensions.toPlaylistDetail
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.model.smartplaylist.HistoryPlaylist
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist
import com.o4x.musical.ui.adapter.home.HomeAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsQueueFragment
import com.o4x.musical.ui.viewmodel.ScrollPositionViewModel
import com.xw.repo.widget.BounceScrollView.OnOverScrollListener
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class HomeFragment : AbsQueueFragment(R.layout.fragment_home) {

    private val scrollPositionViewModel by viewModel<ScrollPositionViewModel> {
        parametersOf(null)
    }

    private lateinit var recentlyAdapter: HomeAdapter
    private lateinit var newAdapter: HomeAdapter
    private lateinit var recentlyLayoutManager: GridLayoutManager
    private lateinit var newLayoutManager: GridLayoutManager


    // Heights //
    private var displayHeight by Delegates.notNull<Int>()
    private var appbarHeight by Delegates.notNull<Int>()
    private var toolbarHeight by Delegates.notNull<Int>()
    private var headerHeight by Delegates.notNull<Int>()


    override fun onResume() {
        super.onResume()

        showAppbar()
        if (scrollPositionViewModel.getPositionValue() < headerHeight) {
            mainActivity.toolbar.setBackgroundColor(Color.TRANSPARENT)
            mainActivity.window.statusBarColor = Color.TRANSPARENT
            mainActivity.appbar.elevation = 0f
            setToolbarTitle(null)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onReloadSubToolbar()
        setUpViews()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
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
        setupPoster()
        setUpBounceScrollView()
        setUpQueueView()
        setUpRecentlyView()
        setUpNewView()
        setupEmpty()
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
            navController.toPlaylistDetail(HistoryPlaylist())
        }
        newly_parent.setOnClickListener {
            navController.toPlaylistDetail(LastAddedPlaylist())
        }
        shuffle_btn.setOnClickListener {
            libraryViewModel.shuffleSongs()
        }
        shuffle_btn.backgroundTintList = ColorStateList.valueOf(themeColor())
        shuffle_btn.setColorFilter(
            getPrimaryTextColor(activity, isColorLight(themeColor())),
            PorterDuff.Mode.SRC_IN
        )
        open_queue_button.setBackgroundColor(themeColor())
        open_queue_button.setTextColor(
            getPrimaryTextColor(activity, isColorLight(themeColor()))
        )
    }

    private fun setupPoster() {
        libraryViewModel.getPosterBitmap().observe(viewLifecycleOwner, {
            poster.setImageBitmap(it)
        })
    }


    private fun setUpBounceScrollView() {

        var isStatusFlat = false
        var isAppbarFlat = false

        nested_scroll_view.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->

            scrollPositionViewModel.setPosition(scrollY)

            // Scroll poster
            poster.y =
                ((-scrollY / (displayHeight * 2 / poster.layoutParams.height.toFloat())).toInt())
                    .toFloat()

            // Scroll appbar
            if (scrollY > headerHeight + toolbarHeight && !isAppbarFlat) {
                setToolbarTitle(navController.currentDestination?.label.toString())
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
                    setToolbarTitle(null)
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


        // zooming poster in over scroll
        val params = poster.layoutParams
        val width = params.width
        val height = params.height
        nested_scroll_view.setOnOverScrollListener { _: Boolean, overScrolledDistance: Int ->
            val scale = 1 + overScrolledDistance / displayHeight.toFloat()
            val mParams: ViewGroup.LayoutParams =
                FrameLayout.LayoutParams(
                    width,
                    (height * scale).toInt()
                )
            poster.layoutParams = mParams
        }
    }

    private fun setUpQueueView() {
        queueLayoutManager = linearLayoutManager
        queue_recycler_view.layoutManager = queueLayoutManager
        queueAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            null,
            true
        )
        queue_recycler_view.adapter = queueAdapter

        libraryViewModel.getQueue().observe(viewLifecycleOwner, {
            queue_container.isVisible = it.isNotEmpty()
            val firstLoad = queueAdapter.itemCount == 0
            queueAdapter.swapDataSet(it, MusicPlayerRemote.position)
            if (firstLoad) toCurrentPosition()
        })
    }

    private fun setUpRecentlyView() {
        recentlyLayoutManager = gridLayoutManager
        recently_recycler_view.layoutManager = recentlyLayoutManager
        recentlyAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            gridSize * 2,
            false,
        )
        recently_recycler_view.adapter = recentlyAdapter
        libraryViewModel.getRecentlyPlayed().observe(viewLifecycleOwner, {
            recently_container.isVisible = it.isNotEmpty()
            recentlyAdapter.swapDataSet(it)
        })
    }

    private fun setUpNewView() {
        newLayoutManager = gridLayoutManager
        new_recycler_view.layoutManager = newLayoutManager
        newAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            gridSize * 3,
            false,
        )
        new_recycler_view.adapter = newAdapter

        libraryViewModel.getRecentlyAdded().observe(viewLifecycleOwner, {
            newly_container.isVisible = it.isNotEmpty()
            newAdapter.swapDataSet(it)
        })
    }

    private val gridLayoutManager: GridLayoutManager
        get() {
            val size = gridSize
            return object : GridLayoutManager(activity, size) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                    lp.width = (width / size) - (lp.marginStart * 2 /* for left and right */)
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
                    lp.width = (width / size) - (lp.marginStart * 2 /* for left and right */)
                    lp.height = (lp.width * 1.5).toInt()
                    return super.checkLayoutParams(lp)
                }
            }
        }

    private val gridSize: Int
        get() = resources.getInteger(R.integer.home_grid_columns)


    private fun setupEmpty() {
        libraryViewModel.getSongs().observe(viewLifecycleOwner, {
            empty.isVisible = it.isEmpty()
            shuffle_btn.isVisible = it.isNotEmpty()
        })
    }
}