package com.o4x.musical.ui.adapter.song;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.helper.SortOrder;
import com.o4x.musical.helper.menu.SongMenuHelper;
import com.o4x.musical.interfaces.CabHolder;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.adapter.base.AbsAdapter;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.NavigationUtil;
import com.o4x.musical.prefs.PreferenceUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongAdapter extends AbsAdapter<SongAdapter.ViewHolder, Song> {

    public SongAdapter(AppCompatActivity activity, List<Song> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        this(activity, dataSet, itemLayoutRes, cabHolder, true);
    }

    public SongAdapter(AppCompatActivity activity, List<Song> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder, boolean showSectionName) {
        super(activity, dataSet, itemLayoutRes, cabHolder);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).getId();
    }


    @Override
    protected ViewHolder createViewHolder(@NotNull View view, int viewType) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Song song = dataSet.get(position);

        if (holder.title != null) {
            holder.title.setText(getSongTitle(song));
        }
        if (holder.text != null) {
            holder.text.setText(getSongText(song));
        }
    }

    @Override
    protected void loadImage(Song song, final ViewHolder holder) {
        if (holder.image == null) return;
        getImageLoader(holder)
                .load(song)
                .into(holder.image);
    }

    protected String getSongTitle(Song song) {
        return song.getTitle();
    }

    protected String getSongText(Song song) {
        return MusicUtil.getSongInfoString(song);
    }

    @Override
    protected String getName(Song song) {
        return song.getTitle();
    }

    @NonNull
    @Override
    protected List<Song> getSongList(@NonNull List<Song> data) {
        return data;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
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
