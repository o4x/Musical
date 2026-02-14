package github.o4x.musical.ui.adapter.song;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemState;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;
import github.o4x.musical.R;
import github.o4x.musical.helper.MusicPlayerRemote;
import github.o4x.musical.interfaces.CabHolder;
import github.o4x.musical.model.Song;
import github.o4x.musical.util.ViewUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import com.o4x.appthemehelper.extensions.ColorExtKt;
import com.o4x.appthemehelper.util.ColorUtil;

public class PlayingQueueAdapter extends SongAdapter implements DraggableItemAdapter<PlayingQueueAdapter.ViewHolder> {

    protected static final int HISTORY = 0;
    protected static final int CURRENT = 1;
    protected static final int UP_NEXT = 2;

    protected int current;

    public PlayingQueueAdapter(AppCompatActivity activity, List<Song> dataSet, int current, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, cabHolder);
        this.current = current;
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NotNull View view, int viewType) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        customizeItem(holder, position);
    }

    protected void customizeItem(@NonNull SongAdapter.ViewHolder holder, int position) {

        if (holder.imageText != null) {
            holder.imageText.setText(String.valueOf(position - current));
        }
        if (getItemType(position) == HISTORY) {
            setAlpha(holder, 0.5f);
        } else {
            setAlpha(holder, 1f);
        }

        if (holder.title != null && holder.text != null) {
            final Typeface typeface = getItemType(position) == CURRENT ?
                    Typeface.DEFAULT_BOLD : Typeface.DEFAULT;
            holder.title.setTypeface(typeface);
            holder.text.setTypeface(typeface);
        }
    }

    protected int getItemType(int position) {
        if (position < current) {
            return HISTORY;
        } else if (position > current) {
            return UP_NEXT;
        }
        return CURRENT;
    }

    public void swapDataSet(List<Song> dataSet, int position) {
        this.dataSet = dataSet;
        current = position;
        notifyDataSetChanged();
    }

    public void setCurrent(int current) {
        this.current = current;
        notifyDataSetChanged();
    }

    private void setAlpha(SongAdapter.ViewHolder holder, float alpha) {
        if (holder.image != null) {
            holder.image.setAlpha(alpha);
        }
        if (holder.title != null) {
            holder.title.setAlpha(alpha);
        }
        if (holder.text != null) {
            holder.text.setAlpha(alpha);
        }
        if (holder.imageText != null) {
            holder.imageText.setAlpha(alpha);
        }
    }

    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        return ViewUtil.hitTest(holder.imageText, holder.itemView, x, y);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        MusicPlayerRemote.moveSong(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    @Override
    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(position - current);
    }

    public class ViewHolder extends SongAdapter.ViewHolder implements DraggableItemViewHolder {
        private final DraggableItemState draggableItemState = new DraggableItemState();

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setBackgroundColor(ColorUtil.INSTANCE.withAlpha(
                    ColorExtKt.backgroundColor(activity), 0.8f));

            if (imageText != null) {
                imageText.setVisibility(View.VISIBLE);
                // Set this for not focus in parent
                imageText.setOnTouchListener((view, motionEvent) -> true);
            }
            if (image != null) {
                image.setVisibility(View.GONE);
            }
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_playing_queue_song;
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
        public void setDragStateFlags(@DraggableItemStateFlags int flags) {
            draggableItemState.setFlags(flags);
        }

        @Override
        @DraggableItemStateFlags
        public int getDragStateFlags() {
            return draggableItemState.getFlags();
        }

        @NonNull
        @Override
        public DraggableItemState getDragState() {
            return draggableItemState;
        }
    }
}
