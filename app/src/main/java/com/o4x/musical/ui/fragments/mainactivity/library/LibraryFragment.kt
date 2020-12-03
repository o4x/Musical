package com.o4x.musical.ui.fragments.mainactivity.library

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import code.name.monkey.appthemehelper.extensions.accentColor
import code.name.monkey.appthemehelper.extensions.surfaceColor
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentLibraryBinding
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.SortOrder
import com.o4x.musical.ui.adapter.MusicLibraryPagerAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.ui.fragments.mainactivity.library.pager.*
import com.o4x.musical.prefs.PreferenceUtil
import com.o4x.musical.prefs.PreferenceUtil.lastPage
import com.o4x.musical.prefs.PreferenceUtil.libraryCategory
import com.o4x.musical.prefs.PreferenceUtil.registerOnSharedPreferenceChangedListener
import com.o4x.musical.prefs.PreferenceUtil.rememberLastTab
import com.o4x.musical.prefs.PreferenceUtil.unregisterOnSharedPreferenceChangedListener
import com.o4x.musical.util.Util

class LibraryFragment : AbsMainActivityFragment(R.layout.fragment_library), OnPageChangeListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var pagerAdapter: MusicLibraryPagerAdapter

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        registerOnSharedPreferenceChangedListener(this)

        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        unregisterOnSharedPreferenceChangedListener(this)
        binding.pager.removeOnPageChangeListener(this)
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewPager()


//        val m = TestAdapter(mainActivity, mutableListOf(), R.layout.item_grid)
//
//        list_view.adapter = m
//
//        libraryViewModel.getSongs().observe(viewLifecycleOwner, {
//            m.swapData(it)
//        })
    }

    override fun onReloadSubToolbar() {
        super.onReloadSubToolbar()
        mainActivity.tabs.visibility = View.VISIBLE
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (PreferenceUtil.LIBRARY_CATEGORIES == key) {
            val current = currentFragment
            pagerAdapter.setCategoryInfos(libraryCategory)
            binding.pager.offscreenPageLimit = pagerAdapter.count
            var position = current?.let { pagerAdapter.getItemPosition(it) }
            position?.let {
                if (it < 0) position = 0
                binding.pager.currentItem = it
                lastPage = it
            }
            updateTabVisibility()
        }
    }

    private fun setUpViewPager() {
        pagerAdapter = MusicLibraryPagerAdapter(mainActivity, childFragmentManager)
        binding.pager.adapter = pagerAdapter
        binding.pager.offscreenPageLimit = pagerAdapter.count
        mainActivity.tabs.setupWithViewPager(binding.pager)
        val primaryColor = surfaceColor()
        val normalColor = ToolbarContentTintHelper.toolbarSubtitleColor(mainActivity, primaryColor)
        val selectedColor = ToolbarContentTintHelper.toolbarTitleColor(mainActivity, primaryColor)
        mainActivity.tabs.setBackgroundColor(primaryColor)
        mainActivity.tabs.setTabTextColors(normalColor, selectedColor)
        mainActivity.tabs.setSelectedTabIndicatorColor(accentColor())
        updateTabVisibility()
        if (rememberLastTab()) {
            binding.pager.currentItem = lastPage
            mainActivity.tabs.setScrollPosition(lastPage,0f,true)
        }
        binding.pager.addOnPageChangeListener(this)
    }

    private fun updateTabVisibility() {
        // hide the tab bar when only a single tab is visible
        mainActivity.tabs.visibility =
            if (pagerAdapter.count == 1) View.GONE else View.VISIBLE
    }

    private val currentFragment: Fragment?
        get() = pagerAdapter.getFragment(binding.pager.currentItem)

    private val isPlaylistPage: Boolean
        get() = currentFragment is PlaylistsFragment

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        if (isPlaylistPage) {
            menu.add(0, R.id.action_new_playlist, 0, R.string.new_playlist_title)
        }
        val currentFragment = currentFragment
        if (currentFragment is AbsLibraryPagerRecyclerViewCustomGridSizeFragment<*, *> &&
            currentFragment.isAdded()) {

            val absLibraryRecyclerViewCustomGridSizeFragment = currentFragment
            val gridSizeItem = menu.findItem(R.id.action_grid_size)
            if (Util.isLandscape(resources)) {
                gridSizeItem.setTitle(R.string.action_grid_size_land)
            }
            setUpGridSizeMenu(absLibraryRecyclerViewCustomGridSizeFragment, gridSizeItem.subMenu)
            setUpSortOrderMenu(absLibraryRecyclerViewCustomGridSizeFragment,
                menu.findItem(R.id.action_sort_order).subMenu)

        } else {
            menu.removeItem(R.id.action_grid_size)
            menu.removeItem(R.id.action_sort_order)
        }
        if (currentFragment is PlaylistsFragment) {
            menu.removeItem(R.id.action_sort_order)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val currentFragment = currentFragment
        if (currentFragment is AbsLibraryPagerRecyclerViewCustomGridSizeFragment<*, *>) {
            val absLibraryRecyclerViewCustomGridSizeFragment = currentFragment
            if (handleGridSizeMenuItem(absLibraryRecyclerViewCustomGridSizeFragment, item)) {
                return true
            }
            if (handleSortOrderMenuItem(absLibraryRecyclerViewCustomGridSizeFragment, item)) {
                return true
            }
        }
        when (item.itemId) {
            R.id.action_shuffle_all -> {
                libraryViewModel.getSongs().value?.let {
                    MusicPlayerRemote.openAndShuffleQueue(it, true)
                }
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

    private fun setUpGridSizeMenu(
        fragment: AbsLibraryPagerRecyclerViewCustomGridSizeFragment<*, *>,
        gridSizeMenu: SubMenu
    ) {
        when (fragment.gridSize) {
            1 -> gridSizeMenu.findItem(R.id.action_grid_size_1).isChecked = true
            2 -> gridSizeMenu.findItem(R.id.action_grid_size_2).isChecked = true
            3 -> gridSizeMenu.findItem(R.id.action_grid_size_3).isChecked = true
            4 -> gridSizeMenu.findItem(R.id.action_grid_size_4).isChecked = true
            5 -> gridSizeMenu.findItem(R.id.action_grid_size_5).isChecked = true
            6 -> gridSizeMenu.findItem(R.id.action_grid_size_6).isChecked = true
            7 -> gridSizeMenu.findItem(R.id.action_grid_size_7).isChecked = true
            8 -> gridSizeMenu.findItem(R.id.action_grid_size_8).isChecked = true
        }
        val maxGridSize = fragment.maxGridSize
        if (maxGridSize < 8) {
            gridSizeMenu.findItem(R.id.action_grid_size_8).isVisible = false
        }
        if (maxGridSize < 7) {
            gridSizeMenu.findItem(R.id.action_grid_size_7).isVisible = false
        }
        if (maxGridSize < 6) {
            gridSizeMenu.findItem(R.id.action_grid_size_6).isVisible = false
        }
        if (maxGridSize < 5) {
            gridSizeMenu.findItem(R.id.action_grid_size_5).isVisible = false
        }
        if (maxGridSize < 4) {
            gridSizeMenu.findItem(R.id.action_grid_size_4).isVisible = false
        }
        if (maxGridSize < 3) {
            gridSizeMenu.findItem(R.id.action_grid_size_3).isVisible = false
        }
    }

    private fun handleGridSizeMenuItem(
        fragment: AbsLibraryPagerRecyclerViewCustomGridSizeFragment<*, *>,
        item: MenuItem
    ): Boolean {
        var gridSize = 0
        when (item.itemId) {
            R.id.action_grid_size_1 -> gridSize = 1
            R.id.action_grid_size_2 -> gridSize = 2
            R.id.action_grid_size_3 -> gridSize = 3
            R.id.action_grid_size_4 -> gridSize = 4
            R.id.action_grid_size_5 -> gridSize = 5
            R.id.action_grid_size_6 -> gridSize = 6
            R.id.action_grid_size_7 -> gridSize = 7
            R.id.action_grid_size_8 -> gridSize = 8
        }
        if (gridSize > 0) {
            item.isChecked = true
            fragment.setAndSaveGridSize(gridSize)
            return true
        }
        return false
    }

    private fun setUpSortOrderMenu(
        fragment: AbsLibraryPagerRecyclerViewCustomGridSizeFragment<*, *>,
        sortOrderMenu: SubMenu
    ) {
        val currentSortOrder = fragment.sortOrder
        sortOrderMenu.clear()
        when (fragment) {
            is AlbumsFragment -> {
                sortOrderMenu.add(0,
                    R.id.action_album_sort_order_asc,
                    0,
                    R.string.sort_order_a_z).isChecked =
                    currentSortOrder == SortOrder.AlbumSortOrder.ALBUM_A_Z
                sortOrderMenu.add(0,
                    R.id.action_album_sort_order_desc,
                    1,
                    R.string.sort_order_z_a).isChecked =
                    currentSortOrder == SortOrder.AlbumSortOrder.ALBUM_Z_A
                sortOrderMenu.add(0,
                    R.id.action_album_sort_order_artist,
                    2,
                    R.string.sort_order_artist).isChecked =
                    currentSortOrder == SortOrder.AlbumSortOrder.ALBUM_ARTIST
                sortOrderMenu.add(0,
                    R.id.action_album_sort_order_year,
                    3,
                    R.string.sort_order_year).isChecked =
                    currentSortOrder == SortOrder.AlbumSortOrder.ALBUM_YEAR
            }
            is ArtistsFragment -> {
                sortOrderMenu.add(0,
                    R.id.action_artist_sort_order_asc,
                    0,
                    R.string.sort_order_a_z).isChecked =
                    currentSortOrder == SortOrder.ArtistSortOrder.ARTIST_A_Z
                sortOrderMenu.add(0,
                    R.id.action_artist_sort_order_desc,
                    1,
                    R.string.sort_order_z_a).isChecked =
                    currentSortOrder == SortOrder.ArtistSortOrder.ARTIST_Z_A
            }
            is SongsFragment -> {
                sortOrderMenu.add(0,
                    R.id.action_song_sort_order_asc,
                    0,
                    R.string.sort_order_a_z).isChecked =
                    currentSortOrder == SortOrder.SongSortOrder.SONG_A_Z
                sortOrderMenu.add(0,
                    R.id.action_song_sort_order_desc,
                    1,
                    R.string.sort_order_z_a).isChecked =
                    currentSortOrder == SortOrder.SongSortOrder.SONG_Z_A
                sortOrderMenu.add(0,
                    R.id.action_song_sort_order_artist,
                    2,
                    R.string.sort_order_artist).isChecked =
                    currentSortOrder == SortOrder.SongSortOrder.SONG_ARTIST
                sortOrderMenu.add(0,
                    R.id.action_song_sort_order_album,
                    3,
                    R.string.sort_order_album).isChecked =
                    currentSortOrder == SortOrder.SongSortOrder.SONG_ALBUM
                sortOrderMenu.add(0,
                    R.id.action_song_sort_order_year,
                    4,
                    R.string.sort_order_year).isChecked =
                    currentSortOrder == SortOrder.SongSortOrder.SONG_YEAR
            }
            is GenresFragment -> {
                sortOrderMenu.add(0,
                    R.id.action_genre_sort_order_asc,
                    0,
                    R.string.sort_order_a_z).isChecked =
                    currentSortOrder == SortOrder.GenreSortOrder.GENRE_A_Z
                sortOrderMenu.add(0,
                    R.id.action_genre_sort_order_desc,
                    1,
                    R.string.sort_order_z_a).isChecked =
                    currentSortOrder == SortOrder.GenreSortOrder.GENRE_Z_A
            }
        }
        sortOrderMenu.setGroupCheckable(0, true, true)
    }

    private fun handleSortOrderMenuItem(
        fragment: AbsLibraryPagerRecyclerViewCustomGridSizeFragment<*, *>,
        item: MenuItem
    ): Boolean {
        var sortOrder: String? = null
        when (fragment) {
            is AlbumsFragment -> {
                when (item.itemId) {
                    R.id.action_album_sort_order_asc -> sortOrder = SortOrder.AlbumSortOrder.ALBUM_A_Z
                    R.id.action_album_sort_order_desc -> sortOrder = SortOrder.AlbumSortOrder.ALBUM_Z_A
                    R.id.action_album_sort_order_artist -> sortOrder =
                        SortOrder.AlbumSortOrder.ALBUM_ARTIST
                    R.id.action_album_sort_order_year -> sortOrder = SortOrder.AlbumSortOrder.ALBUM_YEAR
                }
            }
            is ArtistsFragment -> {
                when (item.itemId) {
                    R.id.action_artist_sort_order_asc -> sortOrder =
                        SortOrder.ArtistSortOrder.ARTIST_A_Z
                    R.id.action_artist_sort_order_desc -> sortOrder =
                        SortOrder.ArtistSortOrder.ARTIST_Z_A
                }
            }
            is SongsFragment -> {
                when (item.itemId) {
                    R.id.action_song_sort_order_asc -> sortOrder = SortOrder.SongSortOrder.SONG_A_Z
                    R.id.action_song_sort_order_desc -> sortOrder = SortOrder.SongSortOrder.SONG_Z_A
                    R.id.action_song_sort_order_artist -> sortOrder =
                        SortOrder.SongSortOrder.SONG_ARTIST
                    R.id.action_song_sort_order_album -> sortOrder = SortOrder.SongSortOrder.SONG_ALBUM
                    R.id.action_song_sort_order_year -> sortOrder = SortOrder.SongSortOrder.SONG_YEAR
                }
            }
            is GenresFragment -> {
                when (item.itemId) {
                    R.id.action_genre_sort_order_asc -> sortOrder =
                        SortOrder.GenreSortOrder.GENRE_A_Z
                    R.id.action_genre_sort_order_desc -> sortOrder =
                        SortOrder.GenreSortOrder.GENRE_Z_A
                }
            }
        }
        if (sortOrder != null) {
            item.isChecked = true
            fragment.setAndSaveSortOrder(sortOrder)
            return true
        }
        return false
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        lastPage = position
    }

    override fun onPageScrollStateChanged(state: Int) {
        showAppbar()
    }
}