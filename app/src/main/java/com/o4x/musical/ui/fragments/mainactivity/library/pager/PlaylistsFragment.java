package com.o4x.musical.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.o4x.musical.R;
import com.o4x.musical.misc.OverScrollLinearLayoutManager;
import com.o4x.musical.model.Playlist;
import com.o4x.musical.model.smartplaylist.HistoryPlaylist;
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist;
import com.o4x.musical.model.smartplaylist.TopTracksPlaylist;
import com.o4x.musical.ui.adapter.PlaylistAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsFragment extends AbsLibraryPagerRecyclerViewFragment<PlaylistAdapter, LinearLayoutManager> {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLibraryFragment().getLibraryViewModel().getLegacyPlaylist()
                .observe(getViewLifecycleOwner(), playlists -> {
                    playlists.add(0, new LastAddedPlaylist());
                    playlists.add(0, new HistoryPlaylist());
                    playlists.add(0, new TopTracksPlaylist());

                    getAdapter().swapDataSet(playlists);
                });
    }

    @NonNull
    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new OverScrollLinearLayoutManager(getActivity());
    }

    @NonNull
    @Override
    protected PlaylistAdapter createAdapter() {
        List<Playlist> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();
        return new PlaylistAdapter(getLibraryFragment().getMainActivity(), dataSet, R.layout.item_list_single_row, getLibraryFragment());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_playlists;
    }
}
