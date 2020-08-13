package com.o4x.musical.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.o4x.musical.R;
import com.o4x.musical.imageloader.universalil.UniversalIL;
import com.o4x.musical.imageloader.universalil.palette.PaletteImageLoadingListener;
import com.o4x.musical.network.temp.Lastfmapi.Models.BestMatchesModel;
import com.o4x.musical.ui.activities.tageditor.OnlineAlbumCoverSearchActivity;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.o4x.musical.ui.dialogs.SetTagsDialog;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public class OnlineAlbumAdapter extends RecyclerView.Adapter<OnlineAlbumAdapter.ItemHolder> {

    private OnlineAlbumCoverSearchActivity activity;
    private List<BestMatchesModel.Results> data;


    public OnlineAlbumAdapter(OnlineAlbumCoverSearchActivity activity, List<BestMatchesModel.Results> bestMatchesModels) {
        this.activity = activity;
        data = bestMatchesModels;
    }

    @NotNull
    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull ItemHolder holder, int position) {
        String url = data.get(position).artworkUrl100.replace("100x100", "300x300");
        assert holder.image != null;
        UniversalIL.onlineAlbumImageLoader(url, holder.image, null);
        assert holder.title != null;
        holder.title.setText(data.get(position).trackName);
        assert holder.text != null;
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
            SetTagsDialog.create(
                    new SetTagsDialog.On() {
                        @Override
                        protected void allTags() {
                            super.allTags();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(
                                    OnlineAlbumCoverSearchActivity.EXTRA_RESULT_ALL,
                                    data.get(getAdapterPosition())
                            );
                            activity.setResult(Activity.RESULT_OK, returnIntent);
                            activity.finish();
                        }

                        @Override
                        protected void justImage() {
                            super.justImage();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(
                                    OnlineAlbumCoverSearchActivity.EXTRA_RESULT_COVER,
                                    data.get(getAdapterPosition()).artworkUrl100
                                            .replace("100x100", "500x500")
                            );
                            activity.setResult(Activity.RESULT_OK, returnIntent);
                            activity.finish();
                        }
                    }
            ).show(activity.getSupportFragmentManager(), "Online");
//            activity.updateAlbumArt(
//                    data.get(getAdapterPosition()).artworkUrl100.replace("100x100", "500x500")
//                    /*,mBestMatchesModels.get(getAdapterPosition()).trackName,
//                    mBestMatchesModels.get(getAdapterPosition()).collectionName,
//                    mBestMatchesModels.get(getAdapterPosition()).artistName,
//                    mBestMatchesModels.get(getAdapterPosition()).primaryGenreName,
//                    mBestMatchesModels.get(getAdapterPosition()).releaseDate,
//                    mBestMatchesModels.get(getAdapterPosition()).trackNumber,
//                    mBestMatchesModels.get(getAdapterPosition()).trackCount*/
//            );
        }
    }
}
