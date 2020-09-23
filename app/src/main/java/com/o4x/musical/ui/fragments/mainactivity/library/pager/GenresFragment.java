package com.o4x.musical.ui.fragments.mainactivity.library.pager;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;

import com.o4x.musical.R;
import com.o4x.musical.interfaces.LoaderIds;
import com.o4x.musical.loader.GenreLoader;
import com.o4x.musical.misc.OverScrollGridLayoutManager;
import com.o4x.musical.misc.WrappedAsyncTaskLoader;
import com.o4x.musical.model.Genre;
import com.o4x.musical.ui.adapter.GenreAdapter;
import com.o4x.musical.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class GenresFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<GenreAdapter, GridLayoutManager> implements LoaderManager.LoaderCallbacks<List<Genre>> {

    private static final int LOADER_ID = LoaderIds.GENRES_FRAGMENT;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new OverScrollGridLayoutManager(requireActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected GenreAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        List<Genre> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();
        return new GenreAdapter(
                getLibraryFragment().getMainActivity(),
                dataSet,
                itemLayoutRes,
                loadUsePalette(),
                getLibraryFragment());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_genres;
    }

    @Override
    public void onMediaStoreChanged() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    @NonNull
    public Loader<List<Genre>> onCreateLoader(int id, Bundle args) {
        return new GenresFragment.AsyncGenreLoader(getActivity());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Genre>> loader, List<Genre> data) {
        getAdapter().swapDataSet(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Genre>> loader) {
        getAdapter().swapDataSet(new ArrayList<>());
    }

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getGenreGridSize(requireActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.setGenreGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getGenreGridSizeLand(requireActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.setGenreGridSizeLand(gridSize);
    }

    @Override
    protected void saveUsePalette(boolean usePalette) {
        PreferenceUtil.setGenreColoredFooters(usePalette);
    }

    @Override
    protected boolean loadUsePalette() {
        return PreferenceUtil.genreColoredFooters();
    }

    @Override
    protected void setUsePalette(boolean usePalette) {
        getAdapter().usePalette(usePalette);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    protected String loadSortOrder() {
        return PreferenceUtil.getGenreSortOrder();
    }

    @Override
    protected void saveSortOrder(String sortOrder) {
        PreferenceUtil.setGenreSortOrder(sortOrder);
    }

    @Override
    protected void setSortOrder(String sortOrder) {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private static class AsyncGenreLoader extends WrappedAsyncTaskLoader<List<Genre>> {
        public AsyncGenreLoader(Context context) {
            super(context);
        }

        @Override
        public List<Genre> loadInBackground() {
            return GenreLoader.getAllGenres(getContext());
        }
    }
}
