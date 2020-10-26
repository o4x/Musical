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
import com.o4x.musical.model.Album;
import com.o4x.musical.repository.RealAlbumRepository;
import com.o4x.musical.repository.RealSongRepository;
import com.o4x.musical.ui.adapter.album.AlbumAdapter;
import com.o4x.musical.ui.viewmodel.ReloadType;
import com.o4x.musical.util.PreferenceUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<AlbumAdapter, GridLayoutManager> {

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getLibraryViewModel()
                .getAlbums().observe(getViewLifecycleOwner(), albums -> {
            getAdapter().swapDataSet(albums);
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected GridLayoutManager createLayoutManager() {
        return new OverScrollGridLayoutManager(getActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected AlbumAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);
        List<Album> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();
        return new AlbumAdapter(
                getLibraryFragment().getMainActivity(),
                dataSet,
                itemLayoutRes,
                getLibraryFragment());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_albums;
    }

    @Override
    protected String loadSortOrder() {
        return PreferenceUtil.getAlbumSortOrder();
    }

    @Override
    protected void saveSortOrder(String sortOrder) {
        PreferenceUtil.setAlbumSortOrder(sortOrder);
    }

    @Override
    protected void setSortOrder(String sortOrder) {
        getLibraryViewModel().forceReload(ReloadType.Albums);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getAlbumGridSize(getActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.setAlbumGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getAlbumGridSizeLand(getActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.setAlbumGridSizeLand(gridSize);
    }
}
