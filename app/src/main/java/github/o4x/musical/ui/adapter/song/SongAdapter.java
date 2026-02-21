package github.o4x.musical.ui.adapter.song;

import android.graphics.Typeface;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import github.o4x.musical.R;
import github.o4x.musical.helper.MusicPlayerRemote;
import github.o4x.musical.helper.SortOrder;
import github.o4x.musical.helper.menu.SongMenuHelper;
import github.o4x.musical.model.Song;
import github.o4x.musical.ui.adapter.base.AbsAdapter;
import github.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import github.o4x.musical.util.MusicUtil;
import github.o4x.musical.util.NavigationUtil;
import github.o4x.musical.prefs.PreferenceUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SongAdapter extends AbsAdapter<SongAdapter.ViewHolder, Song> {

    protected static final int CURRENT = 1;
    public boolean boldCurrent = false;

    public SongAdapter(AppCompatActivity activity, List<Song> dataSet, @LayoutRes int itemLayoutRes) {
        this(activity, dataSet, itemLayoutRes, true);
    }

    public SongAdapter(AppCompatActivity activity, List<Song> dataSet, @LayoutRes int itemLayoutRes, boolean showSectionName) {
        super(activity, dataSet, itemLayoutRes);
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

        final Typeface typeface = boldCurrent && getItemType(position) == CURRENT ?
                Typeface.DEFAULT_BOLD : Typeface.DEFAULT;

        if (holder.title != null) {
            holder.title.setText(getSongTitle(song));
            holder.title.setTypeface(typeface);
        }
        if (holder.text != null) {
            holder.text.setText(getSongText(song));
            holder.text.setTypeface(typeface);
        }
    }

    @Override
    protected void loadImage(Song song, final ViewHolder holder) {
        if (holder.image == null) return;
        getImageLoader(holder)
                .load(song)
                .into(holder.image);
    }

    protected int getItemType(int position) {
        return dataSet.get(position).getId() == MusicPlayerRemote.getCurrentSong().getId() ?
                CURRENT : -1;
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
                final int itemId = item.getItemId();
                if (itemId == R.id.action_go_to_album) {
                    NavigationUtil.goToAlbum(activity, getSong().getAlbumId());
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            MusicPlayerRemote.openQueue(dataSet, getRealPosition(), true);
        }
    }
}
