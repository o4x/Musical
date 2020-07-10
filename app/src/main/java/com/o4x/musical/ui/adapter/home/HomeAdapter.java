package com.o4x.musical.ui.adapter.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.o4x.musical.R;
import com.o4x.musical.glide.PhonographColoredTarget;
import com.o4x.musical.glide.SongGlideRequest;
import com.o4x.musical.helper.MusicPlayerRemote;
import com.o4x.musical.model.Album;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.adapter.album.AlbumAdapter;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.o4x.musical.ui.adapter.song.PlayingQueueAdapter;
import com.o4x.musical.ui.adapter.song.SongAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeAdapter extends PlayingQueueAdapter {

    private final Integer limit;

    public HomeAdapter(AppCompatActivity activity, List<Song> dataSet, int current, @LayoutRes int itemLayoutRes, @Nullable Integer limit, boolean usePalette) {
        super(activity, dataSet, current, itemLayoutRes, usePalette, null);
        this.limit = limit;
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
    protected void setAlpha(SongAdapter.ViewHolder holder, float alpha) {
        // We don't need setAlpha
    }

    class ViewHolder extends SongAdapter.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_playing_queue_song;
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
    }

    @Override
    protected void loadAlbumCover(Song song, SongAdapter.ViewHolder holder) {
        if (holder.image == null) return;

        SongGlideRequest.Builder.from(Glide.with(activity), song)
                .checkIgnoreMediaStore(activity)
                .generatePalette(activity).build()
                .into(new PhonographColoredTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        setColors(getAlbumArtistFooterColor(), holder);
                    }

                    @Override
                    public void onColorReady(int color) {
                        if (usePalette)
                            setColors(color, holder);
                        else
                            setColors(getAlbumArtistFooterColor(), holder);
                    }
                });
    }

    protected void setColors(int color, SongAdapter.ViewHolder holder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            if (holder.title != null) {
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
            if (holder.text != null) {
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
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
