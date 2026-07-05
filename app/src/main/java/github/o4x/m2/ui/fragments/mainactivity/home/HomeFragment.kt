package github.o4x.m2.ui.fragments.mainactivity.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import github.o4x.m2.R
import github.o4x.m2.databinding.FragmentHomeBinding
import github.o4x.m2.extensions.toPlaylistDetail
import github.o4x.m2.helper.GridHelper
import github.o4x.m2.helper.homeGridSize
import github.o4x.m2.model.smartplaylist.HistoryPlaylist
import github.o4x.m2.model.smartplaylist.LastAddedPlaylist
import github.o4x.m2.ui.adapter.home.HomeAdapter
import github.o4x.m2.ui.adapter.home.HomeQueueRowAdapter
import github.o4x.m2.ui.adapter.home.HomeSectionTitleAdapter
import github.o4x.m2.ui.adapter.home.HomeSpacerAdapter
import github.o4x.m2.ui.dialogs.CreatePlaylistDialog
import github.o4x.m2.ui.dialogs.SleepTimerDialog
import github.o4x.m2.ui.fragments.mainactivity.AbsQueueFragment
import github.o4x.m2.ui.viewmodel.HomeHeaderViewModel
import github.o4x.m2.ui.viewmodel.ScrollPositionViewModel
import github.o4x.m2.util.Util
import github.o4x.m2.util.BlurViewUtils.setupBlur
import github.o4x.m2.util.ViewInsetsUtils.applyAppBarPadding
import github.o4x.m2.util.ViewInsetsUtils.applySystemBarsPadding
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : AbsQueueFragment(R.layout.fragment_home), MenuProvider {

    private val posterViewModel by activityViewModel<HomeHeaderViewModel>()
    private val scrollPositionViewModel by viewModel<ScrollPositionViewModel>()

    private lateinit var concatAdapter: ConcatAdapter
    private lateinit var queueTitleAdapter: HomeSectionTitleAdapter
    private lateinit var queueRowAdapter: HomeQueueRowAdapter
    private lateinit var recentlyTitleAdapter: HomeSectionTitleAdapter
    private lateinit var recentlyAdapter: HomeAdapter
    private lateinit var newTitleAdapter: HomeSectionTitleAdapter
    private lateinit var newAdapter: HomeAdapter

    private lateinit var queueSectionRecycler: RecyclerView

    // Heights //
    private var displayHeight = 0
    private var posterHeight = 0

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override val queueRecyclerView: RecyclerView
        get() = queueSectionRecycler

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.setSupportActionBar(binding.toolbar)
        binding.appbar.applySystemBarsPadding(applyTop = true)
        binding.appbarBlur.setupBlur(requireActivity().window, binding.blurTarget)
        binding.homeRecyclerView.applyAppBarPadding(withMiniPlayer = true)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        mainActivity.addMusicServiceEventListener(posterViewModel)
        setUpViews()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_home, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_search -> mainActivity.openSearch()
            R.id.action_new_playlist -> CreatePlaylistDialog.create().show(childFragmentManager, "CREATE_PLAYLIST")
            R.id.nav_queue -> navController.navigate(R.id.action_to_queue)
            R.id.nav_library -> navController.navigate(R.id.action_to_library)
            R.id.nav_folders -> navController.navigate(R.id.action_to_folders)
            R.id.nav_timer -> SleepTimerDialog.create().show(childFragmentManager, "SLEEP_TIMER")
            R.id.nav_settings -> navController.navigate(R.id.settings_activity)
            else -> return false
        }
        return true
    }

    private fun setUpViews() {
        setUpHeights()
        setupPoster()
        setUpQueueSection()
        setUpRecentlySection()
        setUpNewSection()
        setUpHomeRecyclerView()
        setupEmpty()
    }

    private fun setUpHeights() {
        displayHeight = Util.getScreenHeight()
        posterHeight = (displayHeight / 1.5f).toInt()
        binding.poster.updateLayoutParams { height = posterHeight }
    }

    private fun setupPoster() {
        posterViewModel.getPosterBitmap().observe(viewLifecycleOwner) {
            posterViewModel.calculateBitmap(binding.poster, it, Util.getScreenWidth(), posterHeight)
        }
    }

    override fun initQueueView() {
        queueSectionRecycler = RecyclerView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val verticalPadding = (4 * resources.displayMetrics.density).toInt()
            setPadding(0, verticalPadding, 0, verticalPadding)
            clipToPadding = false
            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_ALWAYS
        }
        queueLayoutManager = GridHelper.linearLayoutManager(requireContext())
        queueAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            null,
            true
        )
        queueSectionRecycler.apply {
            layoutManager = queueLayoutManager
            adapter = queueAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(10)
        }
    }

    private fun setUpQueueSection() {
        queueTitleAdapter = HomeSectionTitleAdapter(
            R.string.playing_queue,
            false,
            { navController.navigate(R.id.action_to_queue) },
            { libraryViewModel.shuffleSongs() }
        ).apply { visible = true }
        queueRowAdapter = HomeQueueRowAdapter(queueSectionRecycler, queueRowHeight())
        playerViewModel.queue.observe(viewLifecycleOwner) {
            queueRowAdapter.showEmpty = it.isEmpty()
        }
    }

    private fun queueRowHeight(): Int {
        // Mirror the card size GridHelper's layout managers compute, so the row
        // keeps the strip's height while the empty message is shown.
        val density = resources.displayMetrics.density
        val cardMargin = (2 * density).toInt()
        val cardWidth = Util.getScreenWidth() / requireContext().homeGridSize() - cardMargin * 2
        val cardHeight = (cardWidth * 1.5).toInt()
        val stripPadding = (8 * density).toInt()
        return cardHeight + cardMargin * 2 + stripPadding
    }

    private fun setUpRecentlySection() {
        recentlyTitleAdapter = HomeSectionTitleAdapter(
            R.string.recently_played,
            true,
            { navController.toPlaylistDetail(HistoryPlaylist()) }
        )
        recentlyAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            requireContext().homeGridSize() * 2,
            false,
        )
        libraryViewModel.getRecentlyPlayed().observe(viewLifecycleOwner) {
            recentlyAdapter.swapDataSet(it)
            recentlyTitleAdapter.visible = it.isNotEmpty()
        }
    }

    private fun setUpNewSection() {
        newTitleAdapter = HomeSectionTitleAdapter(
            R.string.recently_added,
            true,
            { navController.toPlaylistDetail(LastAddedPlaylist()) }
        )
        newAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            requireContext().homeGridSize() * 3,
            false,
        )
        libraryViewModel.getRecentlyAdded().observe(viewLifecycleOwner) {
            newAdapter.swapDataSet(it)
            newTitleAdapter.visible = it.isNotEmpty()
        }
    }

    private fun setUpHomeRecyclerView() {
        // Song cards sit directly in the home recycler (one grid cell each), so
        // they are created and bound lazily as rows scroll into view instead of
        // a whole section being inflated at once.
        concatAdapter = ConcatAdapter(
            HomeSpacerAdapter(displayHeight / 3),
            queueTitleAdapter,
            queueRowAdapter,
            recentlyTitleAdapter,
            recentlyAdapter,
            newTitleAdapter,
            newAdapter
        )
        binding.homeRecyclerView.apply {
            layoutManager = createHomeLayoutManager()
            adapter = concatAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    scrollPositionViewModel.addPosition(dy)
                    val scrollY = scrollPositionViewModel.getPositionValue()
                    binding.poster.translationY = -scrollY / (displayHeight * 2f / posterHeight)
                }
            })
        }
    }

    private fun createHomeLayoutManager(): GridLayoutManager {
        val spanCount = requireContext().homeGridSize()
        val layoutManager = object : GridLayoutManager(requireContext(), spanCount) {
            // Song cards are the only children that are match_parent in both
            // dimensions; force them to the same cell size GridHelper gives the
            // nested recyclers. Full-span rows keep their own params.
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                if (lp.width == RecyclerView.LayoutParams.MATCH_PARENT &&
                    lp.height == RecyclerView.LayoutParams.MATCH_PARENT
                ) {
                    lp.width = (width / spanCount) - (lp.marginStart * 2)
                    lp.height = (lp.width * 1.5).toInt()
                }
                return super.checkLayoutParams(lp)
            }
        }
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                var localPosition = position
                for (adapter in concatAdapter.adapters) {
                    val count = adapter.itemCount
                    if (localPosition < count) {
                        return if (adapter is HomeAdapter) 1 else spanCount
                    }
                    localPosition -= count
                }
                return spanCount
            }
        }
        return layoutManager
    }

    private fun setupEmpty() {
        binding.empty.isVisible = false

        libraryViewModel.isLoading.observe(viewLifecycleOwner) { isNowLoading ->
            if (!isNowLoading) {
                binding.empty.isVisible = libraryViewModel.getSongs().value?.isEmpty() == true
            }
        }

        libraryViewModel.getSongs().observe(viewLifecycleOwner) { songs ->
            if (libraryViewModel.isLoading.value == false) {
                binding.empty.isVisible = songs.isEmpty()
            }
        }
    }
}
