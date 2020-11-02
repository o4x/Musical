package com.o4x.musical.ui.adapter.song;

import android.graphics.Typeface;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.interfaces.CabHolder;
import com.o4x.musical.model.Song;
import com.o4x.musical.util.MusicUtil;
import com.o4x.musical.util.NavigationUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import code.name.monkey.appthemehelper.ThemeStore;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistSongAdapter extends AbsOffsetSongAdapter {

    protected static final int CURRENT = 1;

    public PlaylistSongAdapter(AppCompatActivity activity, @NonNull List<Song> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, cabHolder, false);
        setMultiSelectMenuRes(R.menu.menu_cannot_delete_single_songs_playlist_songs_selection);
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NotNull View view, int viewType) {
        return new PlaylistSongAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongAdapter.ViewHolder holder, int position) {
        if (holder.getItemViewType() == OFFSET_ITEM) {
            int textColor = ThemeStore.Companion.textColorSecondary(activity);
            if (holder.title != null) {
                holder.title.setText(MusicUtil.getPlaylistInfoString(activity, dataSet));
                holder.title.setTextColor(textColor);
            }
            if (holder.text != null) {
                holder.text.setVisibility(View.GONE);
            }
            if (holder.menu != null) {
                holder.menu.setVisibility(View.GONE);
            }
            if (holder.image != null) {
                final int padding = activity.getResources().getDimensionPixelSize(R.dimen.default_item_margin) / 2;
                holder.image.setPadding(padding, padding, padding, padding);
                holder.image.setColorFilter(textColor);
                holder.image.setImageResource(R.drawable.ic_timer_white_24dp);
            }
            if (holder.dragView != null) {
                holder.dragView.setVisibility(View.GONE);
            }
        } else {
            position -= 1;
            super.onBindViewHolder(holder, position);

            if (holder.title != null && holder.text != null) {
                final Typeface typeface = getItemType(position) == CURRENT ?
                        Typeface.DEFAULT_BOLD : Typeface.DEFAULT;
                holder.title.setTypeface(typeface);
                holder.text.setTypeface(typeface);
            }
        }
    }

    protected int getItemType(int position) {
        return dataSet.get(position).getId() == MusicPlayerRemote.getCurrentSong().getId() ?
            CURRENT : -1;
    }

    public class ViewHolder extends AbsOffsetSongAdapter.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_cannot_delete_single_songs_playlist_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.action_go_to_album) {
                NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition() - 1).getAlbumId());
                return true;
            }
            return super.onSongMenuItemClick(item);
        }
    }
}
