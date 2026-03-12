package github.o4x.musical.ui.fragments.mainactivity.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.net.toFile
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import github.o4x.musical.App
import github.o4x.musical.R
import github.o4x.musical.databinding.FragmentHomeBinding
import github.o4x.musical.extensions.toPlaylistDetail
import github.o4x.musical.helper.GridHelper
import github.o4x.musical.helper.homeGridSize
import github.o4x.musical.model.smartplaylist.HistoryPlaylist
import github.o4x.musical.model.smartplaylist.LastAddedPlaylist
import github.o4x.musical.prefs.HomeHeaderPref
import github.o4x.musical.ui.activities.MusicPickerActivity
import github.o4x.musical.ui.adapter.home.HomeAdapter
import github.o4x.musical.ui.dialogs.CreatePlaylistDialog
import github.o4x.musical.ui.fragments.mainactivity.AbsQueueFragment
import github.o4x.musical.ui.viewmodel.HomeHeaderViewModel
import github.o4x.musical.ui.viewmodel.ScrollPositionViewModel
import github.o4x.musical.util.MusicUtil
import github.o4x.musical.util.Util
import github.o4x.musical.util.ViewInsetsUtils.applyAppBarPadding
import github.o4x.musical.util.ViewInsetsUtils.applySystemBarsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class HomeFragment : AbsQueueFragment(R.layout.fragment_home), MenuProvider {

    private val posterViewModel by activityViewModel<HomeHeaderViewModel>()
    private val scrollPositionViewModel by viewModel<ScrollPositionViewModel>()

    private lateinit var recentlyAdapter: HomeAdapter
    private lateinit var newAdapter: HomeAdapter
    private lateinit var recentlyLayoutManager: GridLayoutManager
    private lateinit var newLayoutManager: GridLayoutManager

    // Heights //
    private var displayHeight = 0
    private var headerHeight = 0
    private var posterHeight = 0

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override val queueRecyclerView: RecyclerView
        get() = binding.queueRecyclerView

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
        binding.nestedScrollView.applyAppBarPadding()
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
            R.id.action_reset_header -> HomeHeaderPref.setDefault()
            R.id.action_use_custom -> startHomeHeaderImagePicker()
            R.id.action_use_song -> {
                val myIntent = Intent(
                    requireContext(),
                    MusicPickerActivity::class.java
                )
                startActivityForResult(myIntent, REQUEST_CODE_SELECT_SONG)
            }
            R.id.action_new_playlist -> CreatePlaylistDialog.create().show(childFragmentManager, "CREATE_PLAYLIST")
            R.id.nav_queue -> navController.navigate(R.id.action_to_queue)
            R.id.nav_library -> navController.navigate(R.id.action_to_library)
            R.id.nav_folders -> navController.navigate(R.id.action_to_folders)
            R.id.nav_timer -> navController.navigate(R.id.action_to_timer)
            R.id.nav_settings -> navController.navigate(R.id.settings_activity)
            else -> return false
        }
        return true
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
        displayHeight = Util.getScreenHeight()
        val params: ViewGroup.LayoutParams = binding.header.layoutParams
        params.height = (displayHeight / 3)
        binding.header.layoutParams = params

        val posterParams = binding.poster.layoutParams
        posterHeight = (displayHeight / 1.5f).toInt()
        posterParams.height = posterHeight
        binding.poster.layoutParams = posterParams

        headerHeight = binding.header.layoutParams.height
    }

    private fun setupButtons() {
        binding.queueParent.setOnClickListener { navController.navigate(R.id.action_to_queue) }
        binding.queueShuffleButton.setOnClickListener { libraryViewModel.shuffleSongs() }
        binding.recentlyParent.setOnClickListener {
            navController.toPlaylistDetail(HistoryPlaylist())
        }
        binding.newlyParent.setOnClickListener {
            navController.toPlaylistDetail(LastAddedPlaylist())
        }
    }

    private fun setupPoster() {
        posterViewModel.getPosterBitmap().observe(viewLifecycleOwner) {
            posterViewModel.calculateBitmap(binding.poster, it, Util.getScreenWidth(), posterHeight)
        }
    }

    private fun setUpBounceScrollView() {
        binding.nestedScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            scrollPositionViewModel.setPosition(scrollY)
            binding.poster.y = ((-scrollY / (displayHeight * 2 / binding.poster.layoutParams.height.toFloat())).toInt()).toFloat()
        }

        val params = binding.poster.layoutParams
        val width = params.width
        val height = params.height
        binding.nestedScrollView.setOnOverScrollListener { _: Boolean, overScrolledDistance: Int ->
            val scale = 1 + overScrolledDistance / displayHeight.toFloat()
            val mParams: ViewGroup.LayoutParams = FrameLayout.LayoutParams(width, (height * scale).toInt())
            binding.poster.layoutParams = mParams
        }
    }

    override fun initQueueView() {
        queueLayoutManager = GridHelper.linearLayoutManager(requireContext())
        binding.queueRecyclerView.apply {
            layoutManager = queueLayoutManager
            setHasFixedSize(true)
            setItemViewCacheSize(10) // Cache performance
        }
        queueAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            null,
            true
        )
        binding.queueRecyclerView.adapter = queueAdapter

        playerViewModel.queue.observe(viewLifecycleOwner) {
            binding.queueContainer.isVisible = it.isNotEmpty()
        }
    }

    private fun setUpRecentlyView() {
        recentlyLayoutManager = GridHelper.gridLayoutManager(requireContext())
        binding.recentlyRecyclerView.apply {
            layoutManager = recentlyLayoutManager
            setHasFixedSize(true)
        }
        recentlyAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            requireContext().homeGridSize() * 2,
            false,
        )
        binding.recentlyRecyclerView.adapter = recentlyAdapter
        libraryViewModel.getRecentlyPlayed().observe(viewLifecycleOwner) {
            binding.recentlyContainer.isVisible = it.isNotEmpty()
            recentlyAdapter.swapDataSet(it)
        }
    }

    private fun setUpNewView() {
        newLayoutManager = GridHelper.gridLayoutManager(requireContext())
        binding.newRecyclerView.apply {
            layoutManager = newLayoutManager
            setHasFixedSize(true)
        }
        newAdapter = HomeAdapter(
            mainActivity,
            ArrayList(),
            0,
            R.layout.item_card_home,
            requireContext().homeGridSize() * 3,
            false,
        )
        binding.newRecyclerView.adapter = newAdapter

        libraryViewModel.getRecentlyAdded().observe(viewLifecycleOwner) {
            binding.newlyContainer.isVisible = it.isNotEmpty()
            newAdapter.swapDataSet(it)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SELECT_IMAGE -> if (resultCode == Activity.RESULT_OK) {
                data?.data?.let { uri ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (headerDir.isDirectory) {
                            try {
                                val newImage = uri.toFile()
                                headerDir.listFiles()?.forEach { image ->
                                    if (image != newImage) image.delete()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    HomeHeaderPref.customImagePath = uri.toString()
                }
            }
            REQUEST_CODE_SELECT_SONG -> if (resultCode == Activity.RESULT_OK) {
                data?.data?.let {
                    HomeHeaderPref.imageSongID = MusicUtil.getSongIDFromFileUri(it)
                }
            }
        }
    }

    private val headerDir = File(App.getContext().filesDir, "/home_header/")

    private fun startHomeHeaderImagePicker() {
        ImagePicker.with(this)
            .saveDir(headerDir)
            .galleryOnly()
            .crop()
            .start(REQUEST_CODE_SELECT_IMAGE)
    }

    companion object {
        const val REQUEST_CODE_SELECT_IMAGE = 1600
        const val REQUEST_CODE_SELECT_SONG = 1700
    }
}
