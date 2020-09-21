package com.o4x.musical.ui.adapter.artist;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.o4x.musical.R;
import com.o4x.musical.helper.SortOrder;
import com.o4x.musical.helper.menu.SongsMenuHelper;
import com.o4x.musical.interfaces.CabHolder;
import com.o4x.musical.model.Artist;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.adapter.base.AbsMultiSelectAdapter;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.o4x.musical.util.ColorCoverUtil;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.util.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAdapter extends AbsMultiSelectAdapter<ArtistAdapter.ViewHolder, Artist> implements FastScrollRecyclerView.SectionedAdapter {

    protected final AppCompatActivity activity;
    protected List<Artist> dataSet;

    protected int itemLayoutRes;

    protected boolean usePalette = false;

    public ArtistAdapter(@NonNull AppCompatActivity activity, List<Artist> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;
        setHasStableIds(true);
    }

    public void swapDataSet(List<Artist> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public List<Artist> getDataSet() {
        return dataSet;
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).getId();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view);
    }

    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Artist artist = dataSet.get(position);

        boolean isChecked = isChecked(artist);
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
            holder.title.setText(artist.getName());
        }
        if (holder.text != null) {
            holder.text.setText(MusicUtil.getArtistInfoString(activity, artist));
        }
        holder.itemView.setActivated(isChecked(artist));

        loadArtistImage(artist, holder);
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

    protected void loadArtistImage(Artist artist, final ViewHolder holder) {
        if (holder.image == null) return;
        holder.image.setImageBitmap(ColorCoverUtil.createSquareCoverWithText(activity, artist.getName(), artist.getId(), 300));
//        UniversalIL.artistImageLoader(
//                artist,
//                holder.image,
//                new PaletteImageLoadingListener() {
//                    @Override
//                    public void onColorReady(int color) {
//                        if (usePalette)
//                            setColors(color, holder);
//                        else
//                            setColors(getDefaultFooterColor(activity), holder);
//                    }
//                }
//        );
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Artist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Artist artist) {
        return artist.getName();
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull List<Artist> selection) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
    }

    @NonNull
    private List<Song> getSongList(@NonNull List<Artist> artists) {
        final List<Song> songs = new ArrayList<>();
        for (Artist artist : artists) {
            songs.addAll(artist.getSongs()); // maybe async in future?
        }
        return songs;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        @Nullable String sectionName = null;
        switch (PreferenceUtil.getArtistSortOrder()) {
            case SortOrder.ArtistSortOrder.ARTIST_A_Z:
            case SortOrder.ArtistSortOrder.ARTIST_Z_A:
                sectionName = dataSet.get(position).getName();
                break;
        }

        return MusicUtil.getSectionName(sectionName);
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setImageTransitionName(activity.getString(R.string.transition_artist_image));
            if (menu != null) {
                menu.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] artistPairs = new Pair[]{
                        Pair.create(image,
                                activity.getResources().getString(R.string.transition_artist_image)
                        )};
                NavigationUtil.goToArtist(activity, dataSet.get(getAdapterPosition()).getId(), artistPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
