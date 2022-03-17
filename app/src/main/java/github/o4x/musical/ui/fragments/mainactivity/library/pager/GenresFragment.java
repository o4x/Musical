package github.o4x.musical.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import github.o4x.musical.R;
import github.o4x.musical.misc.OverScrollGridLayoutManager;
import github.o4x.musical.model.Genre;
import github.o4x.musical.ui.adapter.GenreAdapter;
import github.o4x.musical.ui.viewmodel.ReloadType;
import github.o4x.musical.prefs.PreferenceUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GenresFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<GenreAdapter, GridLayoutManager> {

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLibraryViewModel().getGenre().observe(getViewLifecycleOwner(), genres -> {
            getAdapter().swapDataSet(genres);
        });
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
                getLibraryFragment().getMainActivity());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_genres;
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
        getLibraryViewModel().forceReload(ReloadType.Genres);
    }
}
