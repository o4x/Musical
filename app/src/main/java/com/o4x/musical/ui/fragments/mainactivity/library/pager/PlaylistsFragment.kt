package com.o4x.musical.ui.fragments.mainactivity.library.pager

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.o4x.musical.R
import com.o4x.musical.misc.OverScrollGridLayoutManager
import com.o4x.musical.model.Playlist
import com.o4x.musical.model.smartplaylist.HistoryPlaylist
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist
import com.o4x.musical.model.smartplaylist.TopTracksPlaylist
import com.o4x.musical.ui.adapter.PlaylistAdapter
import com.o4x.musical.ui.viewmodel.ReloadType
import com.o4x.musical.prefs.PreferenceUtil.getPlaylistGridSize
import com.o4x.musical.prefs.PreferenceUtil.getPlaylistGridSizeLand
import com.o4x.musical.prefs.PreferenceUtil.playlistSortOrder
import com.o4x.musical.prefs.PreferenceUtil.setPlaylistGridSize
import com.o4x.musical.prefs.PreferenceUtil.setPlaylistGridSizeLand
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class PlaylistsFragment :
    AbsLibraryPagerRecyclerViewCustomGridSizeFragment<PlaylistAdapter, GridLayoutManager>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        libraryViewModel.getLegacyPlaylist()
            .observe(viewLifecycleOwner, {
                val playlists: MutableList<Playlist> = it.toMutableList()

                playlists.add(0, LastAddedPlaylist())
                playlists.add(0, HistoryPlaylist())
                playlists.add(0, TopTracksPlaylist())
                adapter?.swapDataSet(playlists)
            })
    }

    override fun createLayoutManager(): GridLayoutManager {
        return OverScrollGridLayoutManager(requireActivity(), gridSize)
    }

    override fun createAdapter(): PlaylistAdapter {
        val dataSet = if (adapter == null) ArrayList() else adapter!!.dataSet
        return PlaylistAdapter(
            libraryFragment.mainActivity,
            dataSet,
            itemLayoutRes,
            libraryFragment.mainActivity
        )
    }

    override val emptyMessage: Int
        get() = R.string.no_playlists

    override fun loadGridSize(): Int {
        return getPlaylistGridSize(requireActivity())
    }

    override fun saveGridSize(gridSize: Int) {
        setPlaylistGridSize(gridSize)
    }

    override fun loadGridSizeLand(): Int {
        return getPlaylistGridSizeLand(requireActivity())
    }

    override fun saveGridSizeLand(gridSize: Int) {
        setPlaylistGridSizeLand(gridSize)
    }

    override fun setGridSize(gridSize: Int) {
        layoutManager?.spanCount = gridSize
        adapter?.notifyDataSetChanged()
    }

    override fun loadSortOrder(): String? {
        return playlistSortOrder
    }

    override fun saveSortOrder(sortOrder: String?) {
        playlistSortOrder = sortOrder
    }

    override fun setSortOrder(sortOrder: String?) {
        libraryViewModel.forceReload(ReloadType.Playlists)
    }
}