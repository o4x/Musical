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
import code.name.monkey.appthemehelper.extensions.accentColor
import code.name.monkey.appthemehelper.extensions.surfaceColor
import code.name.monkey.appthemehelper.util.ColorUtil.isColorLight
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getPrimaryTextColor
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentHomeBinding
import com.o4x.musical.extensions.toPlaylistDetail
import com.o4x.musical.model.smartplaylist.HistoryPlaylist
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist
import com.o4x.musical.ui.adapter.home.HomeAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsQueueFragment
import com.o4x.musical.ui.viewmodel.HomeHeaderViewModel
import com.o4x.musical.ui.viewmodel.ScrollPositionViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class   HomeFragment : AbsQueueFragment(R.layout.fragment_home) {

    private val posterViewModel by sharedViewModel<HomeHeaderViewModel>()

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

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivity.removeMusicServiceEventListener(posterViewModel)
        _binding = null
    }

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
        super.onViewCreated(view, savedInstanceState)
        mainActivity.addMusicServiceEventListener(posterViewModel)
        setUpViews()
    }

    override fun showStatusBar() {}

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
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
        setUpRecentlyView()
        setUpNewView()
        setupEmpty()
    }


    private fun setUpHeights() {
        displayHeight = Resources.getSystem().displayMetrics.heightPixels
        var params: ViewGroup.LayoutParams

        // Set up header height
        params = binding.header.layoutParams
        params.height = displayHeight / 3
        binding.header.layoutParams = params

        // Set up poster image height
        params = binding.poster.layoutParams
        params.height = (displayHeight / 1.5f).toInt()
        binding.poster.layoutParams = params


        appbarHeight = appbarHeight()
        toolbarHeight = toolbarHeight()
        // get real header height
        headerHeight = binding.header.layoutParams.height - appbarHeight
    }

    private fun setupButtons() {
        binding.queueParent.setOnClickListener { mainActivity.setMusicChooser(R.id.nav_queue) }
        binding.queueShuffleButton.setOnClickListener {libraryViewModel.shuffleSongs() }
        binding.recentlyParent.setOnClickListener {
            navController.toPlaylistDetail(HistoryPlaylist())
        }
        binding.newlyParent.setOnClickListener {
            navController.toPlaylistDetail(LastAddedPlaylist())
        }


    }

    private fun setupPoster() {
        posterViewModel.getPosterBitmap().observe(viewLifecycleOwner, {
            binding.poster.setImageBitmap(it)
        })
    }


    private fun setUpBounceScrollView() {

        var isStatusFlat = false
        var isAppbarFlat = false

        binding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->

            scrollPositionViewModel.setPosition(scrollY)

            // Scroll poster
            binding.poster.y =
                ((-scrollY / (displayHeight * 2 / binding.poster.layoutParams.height.toFloat())).toInt())
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
                    } else { // Scrolling down
                        mainActivity.appbar.y = min(
                            0f, mainActivity.appbar.y + (oldScrollY - scrollY)
                        )
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
        val params = binding.poster.layoutParams
        val width = params.width
        val height = params.height
        binding.nestedScrollView.setOnOverScrollListener { _: Boolean, overScrolledDistance: Int ->
            val scale = 1 + overScrolledDistance / displayHeight.toFloat()
            val mParams: ViewGroup.LayoutParams =
                FrameLayout.LayoutParams(
                    width,
                    (height * scale).toInt()
                )
            binding.poster.layoutParams = mParams
        }
    }

    override fun initQueueView() {
        queueLayoutManager = linearLayoutManager
        binding.queueRecyclerView.layoutManager = queueLayoutManager
        queueAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            null,
            true
        )
        binding.queueRecyclerView.adapter = queueAdapter

        playerViewModel.queue.observe(viewLifecycleOwner, {
            binding.queueContainer.isVisible = it.isNotEmpty()
        })
    }

    private fun setUpRecentlyView() {
        recentlyLayoutManager = gridLayoutManager
        binding.recentlyRecyclerView.layoutManager = recentlyLayoutManager
        recentlyAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            gridSize * 2,
            false,
        )
        binding.recentlyRecyclerView.adapter = recentlyAdapter
        libraryViewModel.getRecentlyPlayed().observe(viewLifecycleOwner, {
            binding.recentlyContainer.isVisible = it.isNotEmpty()
            recentlyAdapter.swapDataSet(it)
        })
    }

    private fun setUpNewView() {
        newLayoutManager = gridLayoutManager
        binding.newRecyclerView.layoutManager = newLayoutManager
        newAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            gridSize * 3,
            false,
        )
        binding.newRecyclerView.adapter = newAdapter

        libraryViewModel.getRecentlyAdded().observe(viewLifecycleOwner, {
            binding.newlyContainer.isVisible = it.isNotEmpty()
            newAdapter.swapDataSet(it)
        })
    }

    private val gridLayoutManager: GridLayoutManager
        get() {
            val size = gridSize
            return object : GridLayoutManager(serviceActivity, size) {
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
            return object : LinearLayoutManager(serviceActivity, HORIZONTAL, false) {
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
            binding.empty.isVisible = it.isEmpty()
        })
    }
}