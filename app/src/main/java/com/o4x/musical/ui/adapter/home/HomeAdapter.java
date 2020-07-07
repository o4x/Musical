package com.o4x.musical.ui.adapter.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.o4x.musical.R;
import com.o4x.musical.glide.PhonographColoredTarget;
import com.o4x.musical.glide.SongGlideRequest;
import com.o4x.musical.model.Album;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.adapter.album.AlbumAdapter;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private List<Song> dataSet;
    private Context context;
    private boolean usePalette = false;

    public HomeAdapter(List<Song> dataSet, Context context, boolean usePalette) {
        this.dataSet = dataSet;
        this.context = context;
        this.usePalette = usePalette;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Song song = dataSet.get(position);

        if (holder.title != null) {
            holder.title.setText(song.title);
        }
        if (holder.subtitle != null) {
            holder.subtitle.setText(song.artistName);
        }

        loadAlbumCover(song, holder);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    class ViewHolder extends MediaEntryViewHolder {

        @BindView(R.id.image)
        ImageView image;

        @BindView(R.id.title)
        TextView title;

        @BindView(R.id.subtitle)
        TextView subtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
        }
    }

    private void loadAlbumCover(Song song, final ViewHolder holder) {
        if (holder.image == null) return;

        SongGlideRequest.Builder.from(Glide.with(context), song)
                .checkIgnoreMediaStore(context)
                .generatePalette(context).build()
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

    protected void setColors(int color, ViewHolder holder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            if (holder.title != null) {
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(color)));
            }
            if (holder.text != null) {
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(context, ColorUtil.isColorLight(color)));
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
