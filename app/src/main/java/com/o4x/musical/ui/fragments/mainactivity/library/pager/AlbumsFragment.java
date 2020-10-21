package com.o4x.musical.ui.fragments.mainactivity.library.pager;

import android.content.Context;
import android.os.Bundle;

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
import com.o4x.musical.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<AlbumAdapter, GridLayoutManager> implements LoaderManager.LoaderCallbacks<List<Album>> {

    private static final int LOADER_ID = LoaderIds.ALBUMS_FRAGMENT;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
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
        getLoaderManager().restartLoader(LOADER_ID, null, this);
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

    @Override
    public void onMediaStoreChanged() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<List<Album>> onCreateLoader(int id, Bundle args) {
        return new AsyncAlbumLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
        getAdapter().swapDataSet(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Album>> loader) {
        getAdapter().swapDataSet(new ArrayList<>());
    }

    private static class AsyncAlbumLoader extends WrappedAsyncTaskLoader<List<Album>> {
        public AsyncAlbumLoader(Context context) {
            super(context);
        }

        @Override
        public List<Album> loadInBackground() {
            return new RealAlbumRepository(new RealSongRepository(getContext())).albums();
        }
    }
}
