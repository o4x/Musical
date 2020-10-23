package com.o4x.musical.ui.adapter;

import android.util.Log;
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
import com.o4x.musical.ui.adapter.artist.ArtistAdapter;
import com.o4x.musical.ui.adapter.base.AbsAdapter;
import com.o4x.musical.ui.adapter.base.AbsMultiSelectAdapter;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.util.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;

public class GenreAdapter extends AbsAdapter<GenreAdapter.ViewHolder, Genre> {

    public GenreAdapter(@NonNull AppCompatActivity activity, List<Genre> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, cabHolder);
    }


    @Override
    public long getItemId(int position) {
        return dataSet.get(position).hashCode();
    }

    @Override
    protected ViewHolder createViewHolder(@NotNull View view, int viewType) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Genre genre = dataSet.get(position);

        if (holder.title != null) {
            holder.title.setText(genre.getName());
        }
        if (holder.text != null) {
            holder.text.setText(MusicUtil.getGenreInfoString(activity, genre));
        }
    }

    @Override
    protected void loadImage(Genre genre, final ViewHolder holder) {
        if (holder.image == null) return;
        getImageLoader(holder)
                .load(genre)
                .into(holder.image);
    }

    @Override
    protected String getName(Genre genre) {
        return genre.getName();
    }

    @Override
    @NonNull
    protected List<Song> getSongList(@NonNull List<Genre> genres) {
        final List<Song> songs = new ArrayList<>();
        for (Genre genre : genres) {
            songs.addAll(genre.getSongs()); // maybe async in future?
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
                sectionName = dataSet.get(position).getName();
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
