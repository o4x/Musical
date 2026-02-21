package github.o4x.musical.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import github.o4x.musical.R;
import github.o4x.musical.misc.OverScrollGridLayoutManager;
import github.o4x.musical.model.Artist;
import github.o4x.musical.ui.adapter.artist.ArtistAdapter;
import github.o4x.musical.ui.viewmodel.ReloadType;
import github.o4x.musical.prefs.PreferenceUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArtistsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<ArtistAdapter, GridLayoutManager> {

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLibraryViewModel().getArtists().observe(getViewLifecycleOwner(), artists -> {
            getAdapter().swapDataSet(artists);
        });
    }

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new OverScrollGridLayoutManager(getServiceActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected ArtistAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);
        List<Artist> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();
        return new ArtistAdapter(
                getLibraryFragment().getMainActivity(),
                dataSet,
                itemLayoutRes);
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_artists;
    }

    @Override
    protected String loadSortOrder() {
        return PreferenceUtil.getArtistSortOrder();
    }

    @Override
    protected void saveSortOrder(String sortOrder) {
        PreferenceUtil.setArtistSortOrder(sortOrder);
    }

    @Override
    protected void setSortOrder(String sortOrder) {
        getLibraryViewModel().forceReload(ReloadType.Artists);
    }

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getArtistGridSize(requireActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.setArtistGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getArtistGridSizeLand(getServiceActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.setArtistGridSizeLand(gridSize);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();
    }
}
