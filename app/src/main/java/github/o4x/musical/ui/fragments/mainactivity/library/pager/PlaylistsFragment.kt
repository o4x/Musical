package github.o4x.musical.ui.fragments.mainactivity.library.pager

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import github.o4x.musical.R
import github.o4x.musical.misc.OverScrollGridLayoutManager
import github.o4x.musical.model.Playlist
import github.o4x.musical.model.smartplaylist.HistoryPlaylist
import github.o4x.musical.model.smartplaylist.LastAddedPlaylist
import github.o4x.musical.model.smartplaylist.TopTracksPlaylist
import github.o4x.musical.ui.adapter.PlaylistAdapter
import github.o4x.musical.ui.viewmodel.ReloadType
import github.o4x.musical.prefs.PreferenceUtil.getPlaylistGridSize
import github.o4x.musical.prefs.PreferenceUtil.getPlaylistGridSizeLand
import github.o4x.musical.prefs.PreferenceUtil.playlistSortOrder
import github.o4x.musical.prefs.PreferenceUtil.setPlaylistGridSize
import github.o4x.musical.prefs.PreferenceUtil.setPlaylistGridSizeLand
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