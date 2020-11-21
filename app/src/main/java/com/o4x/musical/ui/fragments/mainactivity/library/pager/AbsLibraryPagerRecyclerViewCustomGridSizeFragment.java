package com.o4x.musical.ui.fragments.mainactivity.library.pager;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import com.o4x.musical.R;
import com.o4x.musical.util.Util;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsLibraryPagerRecyclerViewCustomGridSizeFragment<A extends RecyclerView.Adapter<?>, LM extends RecyclerView.LayoutManager> extends AbsLibraryPagerRecyclerViewFragment<A, LM> {
    private int gridSize;
    private String sortOrder;

    private int currentLayoutRes;

    public final int getGridSize() {
        if (gridSize == 0) {
            if (isLandscape()) {
                gridSize = loadGridSizeLand();
            } else {
                gridSize = loadGridSize();
            }
        }
        return gridSize;
    }

    public int getMaxGridSize() {
        if (isLandscape()) {
            return getResources().getInteger(R.integer.max_columns_land);
        } else {
            return getResources().getInteger(R.integer.max_columns);
        }
    }

    public final String getSortOrder() {
        if (sortOrder == null) {
            sortOrder = loadSortOrder();
        }
        return sortOrder;
    }

    public void setAndSaveGridSize(final int gridSize) {
        int oldLayoutRes = getItemLayoutRes();
        this.gridSize = gridSize;
        if (isLandscape()) {
            saveGridSizeLand(gridSize);
        } else {
            saveGridSize(gridSize);
        }
        // only recreate the adapter and layout manager if the layout currentLayoutRes has changed
        if (oldLayoutRes != getItemLayoutRes()) {
            invalidateLayoutManager();
            invalidateAdapter();
        } else {
            setGridSize(gridSize);
        }
    }

    public void setAndSaveSortOrder(final String sortOrder) {
        this.sortOrder = sortOrder;
        saveSortOrder(sortOrder);
        setSortOrder(sortOrder);
    }

    /**
     *
     *
     * @see #getGridSize()
     */
    @LayoutRes
    protected int getItemLayoutRes() {
        if (getGridSize() > getMaxGridSizeForList()) {
            return R.layout.item_grid;
        }
        return R.layout.item_list;
    }

    protected final void notifyLayoutResChanged(@LayoutRes int res) {
        this.currentLayoutRes = res;
        RecyclerView recyclerView = getRecyclerView();
    }

    protected abstract int loadGridSize();

    protected abstract void saveGridSize(int gridColumns);

    protected abstract int loadGridSizeLand();

    protected abstract void saveGridSizeLand(int gridColumns);

    protected abstract void setGridSize(int gridSize);

    protected abstract String loadSortOrder();

    protected abstract void saveSortOrder(String sortOrder);

    protected abstract void setSortOrder(String sortOrder);

    protected int getMaxGridSizeForList() {
        if (isLandscape()) {
            return getServiceActivity().getResources().getInteger(R.integer.default_list_columns_land);
        }
        return getServiceActivity().getResources().getInteger(R.integer.default_list_columns);
    }

    protected final boolean isLandscape() {
        return Util.isLandscape(getResources());
    }
}
