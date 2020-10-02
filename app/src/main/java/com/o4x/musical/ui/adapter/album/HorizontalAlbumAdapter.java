package com.o4x.musical.ui.adapter.album;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.o4x.musical.helper.HorizontalAdapterHelper;
import com.o4x.musical.imageloader.universalil.loader.UniversalIL;
import com.o4x.musical.imageloader.universalil.listener.PaletteImageLoadingListener;
import com.o4x.musical.interfaces.CabHolder;
import com.o4x.musical.model.Album;
import com.o4x.musical.util.MusicUtil;

import java.util.List;

import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.MaterialValueHelper;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class HorizontalAlbumAdapter extends AlbumAdapter {

    public HorizontalAlbumAdapter(@NonNull AppCompatActivity activity, List<Album> dataSet, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, HorizontalAdapterHelper.LAYOUT_RES, usePalette, cabHolder);
    }

    @Override
    protected ViewHolder createViewHolder(View view, int viewType) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        HorizontalAdapterHelper.applyMarginToLayoutParams(activity, params, viewType);
        return new ViewHolder(view);
    }

    @Override
    protected void setColors(int color, ViewHolder holder) {
        if (holder.itemView != null) {
            CardView card=(CardView)holder.itemView;
            card.setCardBackgroundColor(color);
            if (holder.title != null) {
                    holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.INSTANCE.isColorLight(color)));
            }
            if (holder.text != null) {
                    holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.INSTANCE.isColorLight(color)));
            }
        }
    }

    @Override
    protected void loadAlbumCover(Album album, final ViewHolder holder) {
        if (holder.image == null) return;

        new UniversalIL(holder.image,
                new PaletteImageLoadingListener() {
                    @Override
                    public void onColorReady(int color) {
                        if (usePalette)
                            setColors(color, holder);
                        else
                            setColors(getAlbumArtistFooterColor(activity), holder);
                    }
                }).loadImage(album.safeGetFirstSong());
    }

    @Override
    protected String getAlbumText(Album album) {
        return MusicUtil.getYearString(album.getYear());
    }

    @Override
    public int getItemViewType(int position) {
        return HorizontalAdapterHelper.getItemViewtype(position, getItemCount());
    }
}
