package github.o4x.m2.ui.adapter.home;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;

import github.o4x.m2.R;
import github.o4x.m2.helper.MusicPlayerRemote;
import github.o4x.m2.model.Song;
import github.o4x.m2.ui.adapter.song.PlayingQueueAdapter;
import github.o4x.m2.ui.adapter.song.SongAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends PlayingQueueAdapter {

    // Payload marker for rebinds that only need to refresh the current-song
    // styling (the play/pause icon) without reloading the cover image.
    private static final Object PAYLOAD_CURRENT = new Object();

    private final Integer limit;
    private final boolean isQueue;

    public HomeAdapter(AppCompatActivity activity, List<Song> dataSet, int current, @LayoutRes int itemLayoutRes, @Nullable Integer limit, boolean isQueue) {
        super(activity, dataSet, current, itemLayoutRes);
        this.limit = limit;
        this.isQueue = isQueue;
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NotNull View view, int viewType) {
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
    public void swapDataSet(List<Song> newDataSet) {
        // The home sections live inside a ConcatAdapter, where a plain
        // notifyDataSetChanged() invalidates the whole home list and makes every
        // row flash. Dispatch fine-grained updates over the visible (limited)
        // items instead, so unchanged cards are left untouched.
        final List<Song> oldVisible = new ArrayList<>(dataSet.subList(0, getItemCount()));
        dataSet = newDataSet;
        final List<Song> newVisible = new ArrayList<>(newDataSet.subList(0, getItemCount()));
        DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldVisible.size();
            }

            @Override
            public int getNewListSize() {
                return newVisible.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldVisible.get(oldItemPosition).getId() == newVisible.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldVisible.get(oldItemPosition).equals(newVisible.get(newItemPosition));
            }
        }).dispatchUpdatesTo(this);
    }

    @Override
    public void setCurrent(int current) {
        if (!isQueue) {
            super.setCurrent(current);
            return;
        }
        // Only the play/pause icon depends on the current position here (this
        // adapter overrides customizeItem without the number/alpha styling), so
        // a full notifyDataSetChanged would just make the whole strip flash.
        final int oldCurrent = this.current;
        if (oldCurrent == current) return;
        this.current = current;
        if (oldCurrent >= 0 && oldCurrent < getItemCount()) {
            notifyItemChanged(oldCurrent, PAYLOAD_CURRENT);
        }
        if (current >= 0 && current < getItemCount()) {
            notifyItemChanged(current, PAYLOAD_CURRENT);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            customizeItem(holder, position);
        }
    }

    @Override
    protected void customizeItem(@NonNull SongAdapter.ViewHolder holder, int position) {
        if (isQueue) {
            final int visibility = getItemType(position) == CURRENT ?
                    View.VISIBLE : View.GONE;
            if(holder.icon != null) {
                holder.icon.setVisibility(visibility);
                final int background = MusicPlayerRemote.isPlaying() ?
                        R.drawable.ic_play_arrow : R.drawable.ic_pause;
                holder.icon.setBackgroundResource(background);
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
            final int itemId = item.getItemId();
            if (itemId == R.id.action_remove_from_playing_queue) {
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
}
