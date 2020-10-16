package com.o4x.musical.ui.adapter.song;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.afollestad.materialcab.MaterialCab;
import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.helper.SortOrder;
import com.o4x.musical.helper.menu.SongMenuHelper;
import com.o4x.musical.helper.menu.SongsMenuHelper;
import com.o4x.musical.imageloader.universalil.listener.PaletteImageLoadingListener;
import com.o4x.musical.imageloader.universalil.loader.UniversalIL;
import com.o4x.musical.interfaces.CabHolder;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.adapter.base.AbsMultiSelectAdapter;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.util.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongAdapter extends AbsMultiSelectAdapter<SongAdapter.ViewHolder, Song> implements MaterialCab.Callback, FastScrollRecyclerView.SectionedAdapter {

    protected final AppCompatActivity activity;
    protected List<Song> dataSet;

    protected int itemLayoutRes;

    protected boolean usePalette = false;
    protected boolean showSectionName = true;

    public SongAdapter(AppCompatActivity activity, List<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        this(activity, dataSet, itemLayoutRes, usePalette, cabHolder, true);
    }

    public SongAdapter(AppCompatActivity activity, List<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder, boolean showSectionName) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;
        this.showSectionName = showSectionName;
        setHasStableIds(true);
    }

    public void swapDataSet(List<Song> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    public List<Song> getDataSet() {
        return dataSet;
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
        final Song song = dataSet.get(position);

        boolean isChecked = isChecked(song);
        holder.itemView.setActivated(isChecked);

        if (holder.title != null) {
            holder.title.setText(getSongTitle(song));
        }
        if (holder.text != null) {
            holder.text.setText(getSongText(song));
        }

        loadAlbumCover(song, holder);

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
            if (holder.menu != null) {
                holder.menu.setColorFilter(
                        MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.INSTANCE.isColorLight(color)),
                        PorterDuff.Mode.SRC_IN
                );
            }
        }
    }

    protected void loadAlbumCover(Song song, final ViewHolder holder) {
        if (holder.image == null) return;
        new UniversalIL()
                .withListener(new PaletteImageLoadingListener() {
                    @Override
                    public void onColorReady(int color) {
                        if (usePalette)
                            setColors(
                                    color,
                                    holder);
                        else
                            setColors(getDefaultFooterColor(activity), holder);
                    }
                })
                .byThis(song)
                .displayInTo(holder.image);
    }

    protected String getSongTitle(Song song) {
        return song.getTitle();
    }

    protected String getSongText(Song song) {
        return MusicUtil.getSongInfoString(song);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Song getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Song song) {
        return song.getTitle();
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull List<Song> selection) {
        SongsMenuHelper.handleMenuClick(activity, selection, menuItem.getItemId());
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (!showSectionName) {
            return "";
        }

        @Nullable String sectionName = null;
        switch (PreferenceUtil.getSongSortOrder()) {
            case SortOrder.SongSortOrder.SONG_A_Z:
            case SortOrder.SongSortOrder.SONG_Z_A:
                sectionName = dataSet.get(position).getTitle();
                break;
            case SortOrder.SongSortOrder.SONG_ALBUM:
                sectionName = dataSet.get(position).getAlbumName();
                break;
            case SortOrder.SongSortOrder.SONG_ARTIST:
                sectionName = dataSet.get(position).getArtistName();
                break;
            case SortOrder.SongSortOrder.SONG_YEAR:
                return MusicUtil.getYearString(dataSet.get(position).getYear());
        }

        return MusicUtil.getSectionName(sectionName);
    }

    public class ViewHolder extends MediaEntryViewHolder {
        protected int DEFAULT_MENU_RES = SongMenuHelper.MENU_RES;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setImageTransitionName(activity.getString(R.string.transition_album_art));

            if (menu == null) {
                return;
            }
            menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
                @Override
                public Song getSong() {
                    return ViewHolder.this.getSong();
                }

                @Override
                public int getMenuRes() {
                    return getSongMenuRes();
                }

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return onSongMenuItemClick(item) || super.onMenuItemClick(item);
                }
            });
        }

        protected int getRealPosition() {
            return getAdapterPosition();
        }

        protected Song getSong() {
            return dataSet.get(getRealPosition());
        }

        protected int getSongMenuRes() {
            return DEFAULT_MENU_RES;
        }

        protected boolean onSongMenuItemClick(MenuItem item) {
            if (image != null && image.getVisibility() == View.VISIBLE) {
                switch (item.getItemId()) {
                    case R.id.action_go_to_album:
                        NavigationUtil.goToAlbum(activity, getSong().getAlbumId());
                        return true;
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                MusicPlayerRemote.openQueue(dataSet, getRealPosition(), true);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            return toggleChecked(getAdapterPosition());
        }
    }
}
