package github.o4x.musical.ui.adapter.song;

import android.annotation.SuppressLint;
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
import github.o4x.musical.interfaces.CabHolder;
import github.o4x.musical.model.PlaylistSong;
import github.o4x.musical.model.Song;
import github.o4x.musical.ui.dialogs.RemoveFromPlaylistDialog;
import github.o4x.musical.util.ViewUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import com.o4x.appthemehelper.extensions.ColorExtKt;
import com.o4x.appthemehelper.util.ColorUtil;


@SuppressWarnings("unchecked")
public class OrderablePlaylistSongAdapter extends PlaylistSongAdapter implements DraggableItemAdapter<OrderablePlaylistSongAdapter.ViewHolder> {

    private final OnMoveItemListener onMoveItemListener;

    public OrderablePlaylistSongAdapter(@NonNull AppCompatActivity activity, @NonNull List<PlaylistSong> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder, @Nullable OnMoveItemListener onMoveItemListener) {
        super(activity, (List<Song>) (List) dataSet, itemLayoutRes, cabHolder);
        setMultiSelectMenuRes(R.menu.menu_playlists_songs_selection);
        this.onMoveItemListener = onMoveItemListener;
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NotNull View view, int viewType) {
        return new OrderablePlaylistSongAdapter.ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        position--;
        if (position < 0) return -2;
        return ((List<PlaylistSong>) (List) dataSet).get(position).getIdInPlayList(); // important!
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull List<Song> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_remove_from_playlist:
                RemoveFromPlaylistDialog.create((List<PlaylistSong>) (List) selection).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return;
        }
        super.onMultipleItemAction(menuItem, selection);
    }

    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        return onMoveItemListener != null && position > 0 &&
                (ViewUtil.hitTest(holder.dragView, holder.itemView, x, y)
                        || ViewUtil.hitTest(holder.image, holder.itemView, x, y));
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
        return new ItemDraggableRange(1, dataSet.size());
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (onMoveItemListener != null && fromPosition != toPosition) {
            onMoveItemListener.onMoveItem(fromPosition - 1, toPosition - 1);
        }
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return dropPosition > 0;
    }

    @Override
    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }

    public interface OnMoveItemListener {
        void onMoveItem(int fromPosition, int toPosition);
    }

    public class ViewHolder extends PlaylistSongAdapter.ViewHolder implements DraggableItemViewHolder {
        private final DraggableItemState draggableItemState = new DraggableItemState();

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setBackgroundColor(ColorUtil.INSTANCE.withAlpha(
                    ColorExtKt.backgroundColor(activity), 0.8f));

            if (dragView != null) {
                if (onMoveItemListener != null) {
                    dragView.setVisibility(View.VISIBLE);
                    // Set this for not focus in parent
                    dragView.setOnTouchListener((view, motionEvent) -> true);
                } else {
                    dragView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_playlist_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_remove_from_playlist:
                    RemoveFromPlaylistDialog.create((PlaylistSong) getSong()).show(activity.getSupportFragmentManager(), "REMOVE_FROM_PLAYLIST");
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
