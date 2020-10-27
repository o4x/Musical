package com.o4x.musical.ui.fragments.mainactivity.library.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.o4x.musical.R
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.model.Playlist
import com.o4x.musical.model.smartplaylist.HistoryPlaylist
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist
import com.o4x.musical.model.smartplaylist.TopTracksPlaylist
import com.o4x.musical.ui.adapter.PlaylistAdapter
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class PlaylistsFragment :
    AbsLibraryPagerRecyclerViewFragment<PlaylistAdapter, LinearLayoutManager>() {

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

    override fun createLayoutManager(): LinearLayoutManager {
        return OverScrollLinearLayoutManager(requireActivity())
    }

    override fun createAdapter(): PlaylistAdapter {
        val dataSet = if (adapter == null) ArrayList() else adapter!!.dataSet
        return PlaylistAdapter(
            libraryFragment.mainActivity,
            dataSet,
            R.layout.item_list_single_row,
            libraryFragment
        )
    }

    override val emptyMessage: Int
        get() = R.string.no_playlists
}