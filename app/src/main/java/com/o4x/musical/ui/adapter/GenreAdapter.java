package com.o4x.musical.ui.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.o4x.musical.R;
import com.o4x.musical.helper.SortOrder;
import com.o4x.musical.helper.menu.SongsMenuHelper;
import com.o4x.musical.imageloader.universalil.listener.PaletteImageLoadingListener;
import com.o4x.musical.imageloader.universalil.loader.UniversalIL;
import com.o4x.musical.interfaces.CabHolder;
import com.o4x.musical.model.Genre;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.adapter.base.AbsMultiSelectAdapter;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.util.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;

public class GenreAdapter extends AbsMultiSelectAdapter<GenreAdapter.ViewHolder, Genre> implements FastScrollRecyclerView.SectionedAdapter {

    @NonNull
    private final AppCompatActivity activity;
    private List<Genre> dataSet;
    private int itemLayoutRes;

    protected boolean usePalette = false;

    public GenreAdapter(@NonNull AppCompatActivity activity, List<Genre> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;
    }

    public List<Genre> getDataSet() {
        return dataSet;
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    public void swapDataSet(List<Genre> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Genre genre = dataSet.get(position);

        boolean isChecked = isChecked(genre);
        holder.itemView.setActivated(isChecked);

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        if (holder.title != null) {
            holder.title.setText(genre.name);
        }
        if (holder.text != null) {
            holder.text.setText(MusicUtil.getGenreInfoString(activity, genre));
        }
        holder.itemView.setActivated(isChecked(genre));

        loadArtistImage(genre, holder);
    }

    private void setColors(int color, ViewHolder holder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            if (holder.title != null) {
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.INSTANCE.isColorLight(color)));
            }
            if (holder.text != null) {
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.INSTANCE.isColorLight(color)));
            }
        }
    }

    protected void loadArtistImage(Genre genre, final ViewHolder holder) {
        if (holder.image == null) return;
        new UniversalIL(holder.image,
                new PaletteImageLoadingListener() {
                    @Override
                    public void onColorReady(int color) {
                        if (usePalette)
                            setColors(color, holder);
                        else
                            setColors(getDefaultFooterColor(activity), holder);
                    }
                }).loadImage(genre);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Nullable
    @Override
    protected Genre getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Genre genre) {
        return genre.name;
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, List<Genre> selection) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
    }

    @NonNull
    private List<Song> getSongList(@NonNull List<Genre> genres) {
        final List<Song> songs = new ArrayList<>();
        for (Genre genre : genres) {
            songs.addAll(genre.songs); // maybe async in future?
        }
        return songs;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        @Nullable String sectionName = null;
        switch (PreferenceUtil.getGenreSortOrder()) {
            case SortOrder.GenreSortOrder.GENRE_A_Z:
            case SortOrder.GenreSortOrder.GENRE_Z_A:
                sectionName = dataSet.get(position).name;
                break;
        }

        return MusicUtil.getSectionName(sectionName);
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            if (menu != null) {
                menu.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Genre genre = dataSet.get(getAdapterPosition());
                NavigationUtil.goToGenre(activity, genre);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
