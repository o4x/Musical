package com.o4x.musical.ui.adapter.home;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.o4x.musical.R;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.adapter.song.PlayingQueueAdapter;
import com.o4x.musical.ui.adapter.song.SongAdapter;

import java.util.List;

public class HomeAdapter extends PlayingQueueAdapter {

    private final Integer limit;
    private final boolean isQueue;

    public HomeAdapter(AppCompatActivity activity, List<Song> dataSet, int current, @LayoutRes int itemLayoutRes, @Nullable Integer limit, boolean usePalette, boolean isQueue) {
        super(activity, dataSet, current, itemLayoutRes, usePalette, null);
        this.limit = limit;
        this.isQueue = isQueue;
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new HomeAdapter.ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (limit == null) {
            return dataSet.size();
        } else {
            if(dataSet.size() > limit){
                return limit;
            }
            else
            {
                return dataSet.size();
            }
        }
    }

    @Override
    protected void customizeItem(@NonNull SongAdapter.ViewHolder holder, int position) {
        if (isQueue) {
            final int visibility = position == MusicPlayerRemote.getPosition() ?
                    View.VISIBLE : View.GONE;
            if(holder.icon != null) {
                holder.icon.setVisibility(visibility);
                final int background = MusicPlayerRemote.isPlaying() ?
                        R.drawable.ic_play_arrow_white_24dp : R.drawable.ic_pause_white_24dp;
                holder.icon.setBackground(activity.getDrawable(background));
            }
        }
    }

    class ViewHolder extends SongAdapter.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected int getSongMenuRes() {
            if(isQueue) return R.menu.menu_item_playing_queue_song;
            else return R.menu.menu_item_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_remove_from_playing_queue:
                    MusicPlayerRemote.removeFromQueue(getAdapterPosition());
                    return true;
            }
            return super.onSongMenuItemClick(item);
        }

        @Override
        public boolean onLongClick(View view) {
            return menu.callOnClick();
        }
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    public void swapDataSet(List<Song> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public List<Song> getDataSet() {
        return dataSet;
    }


}
