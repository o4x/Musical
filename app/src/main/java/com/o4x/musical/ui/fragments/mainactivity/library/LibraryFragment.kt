package com.o4x.musical.ui.fragments.mainactivity.library

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import code.name.monkey.appthemehelper.ThemeStore.Companion.themeColor
import code.name.monkey.appthemehelper.common.ATHToolbarActivity
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.afollestad.materialcab.MaterialCab
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.tabs.TabLayout
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.SortOrder
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.loader.SongLoader
import com.o4x.musical.ui.activities.SearchActivity
import com.o4x.musical.ui.adapter.MusicLibraryPagerAdapter
import com.o4x.musical.ui.dialogs.CreatePlaylistDialog
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.ui.fragments.mainactivity.library.pager.*
import com.o4x.musical.util.PhonographColorUtil
import com.o4x.musical.util.PreferenceUtil
import com.o4x.musical.util.PreferenceUtil.lastPage
import com.o4x.musical.util.PreferenceUtil.libraryCategory
import com.o4x.musical.util.PreferenceUtil.registerOnSharedPreferenceChangedListener
import com.o4x.musical.util.PreferenceUtil.rememberLastTab
import com.o4x.musical.util.PreferenceUtil.unregisterOnSharedPreferenceChangedListener
import com.o4x.musical.util.Util
import kotlinx.android.synthetic.main.fragment_library.*

class LibraryFragment : AbsMainActivityFragment(), CabHolder, OnPageChangeListener,
    SharedPreferences.OnSharedPreferenceChangeListener {



    private var pagerAdapter: MusicLibraryPagerAdapter? = null
    private var cab: MaterialCab? = null

    override fun getLayout(): Int = R.layout.fragment_library

    override fun onDestroyView() {
        unregisterOnSharedPreferenceChangedListener(this)
        super.onDestroyView()
        pager!!.removeOnPageChangeListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        registerOnSharedPreferenceChangedListener(this)
        mainActivity.setStatusBarColorAuto()
        mainActivity.setNavigationBarColorAuto()
        mainActivity.setTaskDescriptionColorAuto()
        setUpToolbar()
        setUpViewPager()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (PreferenceUtil.LIBRARY_CATEGORIES == key) {
            val current = currentFragment
            pagerAdapter!!.setCategoryInfos(libraryCategory)
            pager!!.offscreenPageLimit = pagerAdapter!!.count - 1
            var position = pagerAdapter!!.getItemPosition(current)
            if (position < 0) position = 0
            pager!!.currentItem = position
            lastPage = position
            updateTabVisibility()
        }
    }

    private fun setUpToolbar() {
        val primaryColor = themeColor(mainActivity)
        appbar!!.setBackgroundColor(primaryColor)
        toolbar!!.setBackgroundColor(primaryColor)
        toolbar!!.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        mainActivity.setTitle(R.string.app_name)
        mainActivity.setSupportActionBar(toolbar)
    }

    private fun setUpViewPager() {
        pagerAdapter = MusicLibraryPagerAdapter(mainActivity, childFragmentManager)
        pager!!.adapter = pagerAdapter
        pager!!.offscreenPageLimit = pagerAdapter!!.count - 1
        tabs!!.setupWithViewPager(pager)
        val primaryColor = themeColor(mainActivity)
        val normalColor = ToolbarContentTintHelper.toolbarSubtitleColor(mainActivity, primaryColor)
        val selectedColor = ToolbarContentTintHelper.toolbarTitleColor(mainActivity, primaryColor)
        //        TabLayoutUtil.setTabIconColors(tabs, normalColor, selectedColor);
        tabs!!.setTabTextColors(normalColor, selectedColor)
        tabs!!.setSelectedTabIndicatorColor(themeColor(mainActivity))
        updateTabVisibility()
        if (rememberLastTab()) {
            pager!!.currentItem = lastPage
        }
        pager!!.addOnPageChangeListener(this)
    }

    private fun updateTabVisibility() {
        // hide the tab bar when only a single tab is visible
        tabs!!.visibility =
            if (pagerAdapter!!.count == 1) View.GONE else View.VISIBLE
    }

    val currentFragment: Fragment
        get() = pagerAdapter!!.getFragment(pager!!.currentItem)
    private val isPlaylistPage: Boolean
        private get() = currentFragment is PlaylistsFragment

    override fun openCab(menuRes: Int, callback: MaterialCab.Callback): MaterialCab {
        if (cab != null && cab!!.isActive) cab!!.finish()
        cab = MaterialCab(mainActivity, R.id.cab_stub)
            .setMenu(menuRes)
            .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
            .setBackgroundColor(PhonographColorUtil.shiftBackgroundColorForLightText(themeColor(
                mainActivity)))
            .start(callback)
        return cab!!
    }

    fun addOnAppBarOffsetChangedListener(onOffsetChangedListener: OnOffsetChangedListener?) {
        appbar!!.addOnOffsetChangedListener(onOffsetChangedListener)
    }

    fun removeOnAppBarOffsetChangedListener(onOffsetChangedListener: OnOffsetChangedListener?) {
        appbar!!.removeOnOffsetChangedListener(onOffsetChangedListener)
    }

    val totalAppBarScrollingRange: Int
        get() = appbar!!.totalScrollRange

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (pager == null) return
        inflater.inflate(R.menu.menu_main, menu)
        if (isPlaylistPage) {
            menu.add(0, R.id.action_new_playlist, 0, R.string.new_playlist_title)
        }
        val currentFragment = currentFragment
        if (currentFragment is AbsLibraryPagerRecyclerViewCustomGridSizeFragment<*, *> && currentFragment.isAdded()) {
            val absLibraryRecyclerViewCustomGridSizeFragment = currentFragment
            val gridSizeItem = menu.findItem(R.id.action_grid_size)
            if (Util.isLandscape(resources)) {
                gridSizeItem.setTitle(R.string.action_grid_size_land)
            }
            setUpGridSizeMenu(absLibraryRecyclerViewCustomGridSizeFragment, gridSizeItem.subMenu)
            menu.findItem(R.id.action_colored_footers).isChecked =
                absLibraryRecyclerViewCustomGridSizeFragment.usePalette()
            menu.findItem(R.id.action_colored_footers).isEnabled =
                absLibraryRecyclerViewCustomGridSizeFragment.canUsePalette()
            setUpSortOrderMenu(absLibraryRecyclerViewCustomGridSizeFragment,
                menu.findItem(R.id.action_sort_order).subMenu)
        } else {
            menu.removeItem(R.id.action_grid_size)
            menu.removeItem(R.id.action_colored_footers)
            menu.removeItem(R.id.action_sort_order)
        }
        val activity = activity ?: return
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(mainActivity,
            toolbar!!,
            menu,
            ATHToolbarActivity.getToolbarBackgroundColor(toolbar))
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val activity = activity ?: return
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(activity, toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (pager == null) return false
        val currentFragment = currentFragment
        if (currentFragment is AbsLibraryPagerRecyclerViewCustomGridSizeFragment<*, *>) {
            val absLibraryRecyclerViewCustomGridSizeFragment = currentFragment
            if (item.itemId == R.id.action_colored_footers) {
                item.isChecked = !item.isChecked
                absLibraryRecyclerViewCustomGridSizeFragment.setAndSaveUsePalette(item.isChecked)
                return true
            }
            if (handleGridSizeMenuItem(absLibraryRecyclerViewCustomGridSizeFragment, item)) {
                return true
            }
            if (handleSortOrderMenuItem(absLibraryRecyclerViewCustomGridSizeFragment, item)) {
                return true
            }
        }
        val id = item.itemId
        when (id) {
            R.id.action_shuffle_all -> {
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(mainActivity), true)
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
            toolbar!!.menu.findItem(R.id.action_colored_footers).isEnabled =
                fragment.canUsePalette()
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
        if (fragment is AlbumsFragment) {
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
        } else if (fragment is ArtistsFragment) {
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
        } else if (fragment is SongsFragment) {
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
        sortOrderMenu.setGroupCheckable(0, true, true)
    }

    private fun handleSortOrderMenuItem(
        fragment: AbsLibraryPagerRecyclerViewCustomGridSizeFragment<*, *>,
        item: MenuItem
    ): Boolean {
        var sortOrder: String? = null
        if (fragment is AlbumsFragment) {
            when (item.itemId) {
                R.id.action_album_sort_order_asc -> sortOrder = SortOrder.AlbumSortOrder.ALBUM_A_Z
                R.id.action_album_sort_order_desc -> sortOrder = SortOrder.AlbumSortOrder.ALBUM_Z_A
                R.id.action_album_sort_order_artist -> sortOrder =
                    SortOrder.AlbumSortOrder.ALBUM_ARTIST
                R.id.action_album_sort_order_year -> sortOrder = SortOrder.AlbumSortOrder.ALBUM_YEAR
            }
        } else if (fragment is ArtistsFragment) {
            when (item.itemId) {
                R.id.action_artist_sort_order_asc -> sortOrder =
                    SortOrder.ArtistSortOrder.ARTIST_A_Z
                R.id.action_artist_sort_order_desc -> sortOrder =
                    SortOrder.ArtistSortOrder.ARTIST_Z_A
            }
        } else if (fragment is SongsFragment) {
            when (item.itemId) {
                R.id.action_song_sort_order_asc -> sortOrder = SortOrder.SongSortOrder.SONG_A_Z
                R.id.action_song_sort_order_desc -> sortOrder = SortOrder.SongSortOrder.SONG_Z_A
                R.id.action_song_sort_order_artist -> sortOrder =
                    SortOrder.SongSortOrder.SONG_ARTIST
                R.id.action_song_sort_order_album -> sortOrder = SortOrder.SongSortOrder.SONG_ALBUM
                R.id.action_song_sort_order_year -> sortOrder = SortOrder.SongSortOrder.SONG_YEAR
            }
        }
        if (sortOrder != null) {
            item.isChecked = true
            fragment.setAndSaveSortOrder(sortOrder)
            return true
        }
        return false
    }

    override fun handleBackPress(): Boolean {
        if (cab != null && cab!!.isActive) {
            cab!!.finish()
            return true
        }
        return false
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        lastPage = position
    }

    override fun onPageScrollStateChanged(state: Int) {}

    companion object {
        fun newInstance(): LibraryFragment {
            return LibraryFragment()
        }
    }
}