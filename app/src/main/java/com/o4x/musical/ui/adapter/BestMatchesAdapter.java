package com.o4x.musical.ui.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.o4x.musical.R;
import com.o4x.musical.imageloader.universalil.UniversalIL;
import com.o4x.musical.network.temp.Lastfmapi.Models.BestMatchesModel;
import com.o4x.musical.ui.activities.tageditor.WebAlbumCoverActivity;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;

import java.util.List;

public class BestMatchesAdapter extends RecyclerView.Adapter<BestMatchesAdapter.ItemHolder> {

    private WebAlbumCoverActivity activity;
    private List<BestMatchesModel.Results> data;



    public BestMatchesAdapter(WebAlbumCoverActivity activity, List<BestMatchesModel.Results> bestMatchesModels) {
        this.activity = activity;
        data = bestMatchesModels;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        String url = data.get(position).artworkUrl100.replace("100x100", "300x300");
        UniversalIL.getImageLoader().displayImage(url, holder.image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                holder.image.setImageResource(R.drawable.default_album_art);
//                int padding = MusicUtils.getDPFromPixel(20);
//                holder.image.setPadding(padding, padding, padding, padding);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.image.setImageResource(R.drawable.default_album_art);
//                int padding = MusicUtils.getDPFromPixel(20);
//                holder.image.setPadding(padding, padding, padding, padding);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                int padding = MusicUtils.getDPFromPixel(0);
//                holder.image.setPadding(padding, padding, padding, padding);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        holder.title.setText(data.get(position).trackName);
        holder.text.setText(data.get(position).artistName);

    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public void updateData(List<BestMatchesModel.Results> resultses) {
        this.data = resultses;
        notifyDataSetChanged();
    }

    public class ItemHolder extends MediaEntryViewHolder {


        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {
            super.onClick(v);
            activity.updateAlbumArt(
                    data.get(getAdapterPosition()).artworkUrl100.replace("100x100", "500x500")
                    /*,mBestMatchesModels.get(getAdapterPosition()).trackName,
                    mBestMatchesModels.get(getAdapterPosition()).collectionName,
                    mBestMatchesModels.get(getAdapterPosition()).artistName,
                    mBestMatchesModels.get(getAdapterPosition()).primaryGenreName,
                    mBestMatchesModels.get(getAdapterPosition()).releaseDate,
                    mBestMatchesModels.get(getAdapterPosition()).trackNumber,
                    mBestMatchesModels.get(getAdapterPosition()).trackCount*/
            );
        }
    }
}
