package com.o4x.musical.ui.fragments.mainactivity.home

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import code.name.monkey.appthemehelper.util.ColorUtil.isColorDark
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
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.imageloader.glide.targets.MusicColoredTargetListener
import com.o4x.musical.interfaces.MusicServiceEventListener
import com.o4x.musical.model.smartplaylist.HistoryPlaylist
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist
import com.o4x.musical.ui.adapter.home.HomeAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.ui.fragments.mainactivity.AbsQueueFragment
import com.o4x.musical.ui.viewmodel.LibraryViewModel
import com.o4x.musical.ui.viewmodel.ScrollPositionViewModel
import com.o4x.musical.util.CoverUtil
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.Util
import com.o4x.musical.util.color.MediaNotificationProcessor
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs
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


    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        showAppbar()
        if (scrollPositionViewModel.getPositionValue() < headerHeight) {
            mainActivity.toolbar.setBackgroundColor(Color.TRANSPARENT)
            mainActivity.window.statusBarColor = Color.TRANSPARENT
            mainActivity.appbar.elevation = 0f
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onReloadSubToolbar()
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
        setupPoster()
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
            ArrayList(),
            0,
            R.layout.item_card_home,
            null,
            true
        )
        queue_recycler_view.adapter = queueAdapter
        queue_recycler_view.itemAnimator = animator

        libraryViewModel.getQueue().observe(viewLifecycleOwner, {
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


    private fun checkIsEmpty() {
        if (empty != null) {
            empty.visibility = if ((queueAdapter.itemCount == 0) and
                (recentlyAdapter.itemCount == 0) and
                (newAdapter.itemCount == 0)
            ) View.VISIBLE else View.GONE
        }
    }
}