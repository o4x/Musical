package com.o4x.musical.ui.fragments.mainactivity.library.pager;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;

import com.o4x.musical.R;
import com.o4x.musical.interfaces.LoaderIds;
import com.o4x.musical.misc.OverScrollGridLayoutManager;
import com.o4x.musical.misc.WrappedAsyncTaskLoader;
import com.o4x.musical.model.Song;
import com.o4x.musical.repository.RealSongRepository;
import com.o4x.musical.ui.adapter.song.SongAdapter;
import com.o4x.musical.ui.viewmodel.ReloadType;
import com.o4x.musical.util.PreferenceUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<SongAdapter, GridLayoutManager> {

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLibraryViewModel().getSongs().observe(getViewLifecycleOwner(), songs -> {
            getAdapter().swapDataSet(songs);
        });
    }

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new OverScrollGridLayoutManager(getActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected SongAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);
        List<Song> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();

        SongAdapter songAdapter;
        songAdapter = new SongAdapter(
                getLibraryFragment().getMainActivity(),
                dataSet,
                itemLayoutRes,
                getLibraryFragment());

        return songAdapter;
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_songs;
    }

    @Override
    protected String loadSortOrder() {
        return PreferenceUtil.getSongSortOrder();
    }

    @Override
    protected void saveSortOrder(String sortOrder) {
        PreferenceUtil.setSongSortOrder(sortOrder);
    }

    @Override
    protected void setSortOrder(String sortOrder) {
        getLibraryViewModel().forceReload(ReloadType.Songs);
    }

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getSongGridSize(getActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.setSongGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getSongGridSizeLand(getActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.setSongGridSizeLand(gridSize);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();
    }
}
