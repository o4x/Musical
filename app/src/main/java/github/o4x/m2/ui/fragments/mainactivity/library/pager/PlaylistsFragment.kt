package github.o4x.m2.ui.fragments.mainactivity.library.pager

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import github.o4x.m2.R
import github.o4x.m2.misc.OverScrollGridLayoutManager
import github.o4x.m2.model.Playlist
import github.o4x.m2.model.smartplaylist.HistoryPlaylist
import github.o4x.m2.model.smartplaylist.LastAddedPlaylist
import github.o4x.m2.model.smartplaylist.TopTracksPlaylist
import github.o4x.m2.ui.adapter.PlaylistAdapter
import github.o4x.m2.ui.viewmodel.ReloadType
import github.o4x.m2.prefs.PreferenceUtil.getPlaylistGridSize
import github.o4x.m2.prefs.PreferenceUtil.getPlaylistGridSizeLand
import github.o4x.m2.prefs.PreferenceUtil.playlistSortOrder
import github.o4x.m2.prefs.PreferenceUtil.setPlaylistGridSize
import github.o4x.m2.prefs.PreferenceUtil.setPlaylistGridSizeLand
import java.util.*

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
            itemLayoutRes
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