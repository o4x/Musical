package com.o4x.musical.ui.adapter.online;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.o4x.musical.R;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AbsSearchOnlineActivity;
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.o4x.musical.ui.dialogs.SetTagsDialog;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public abstract class SearchOnlineAdapter<A extends AbsSearchOnlineActivity, RM extends List<? extends Serializable>>
        extends RecyclerView.Adapter<SearchOnlineAdapter.ViewHolder> {

    private A activity;
    protected RM data;


    public SearchOnlineAdapter(A activity, RM resultsModels) {
        this.activity = activity;
        data = resultsModels;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public void updateData(RM results) {
        this.data = results;
        notifyDataSetChanged();
    }

    public class ViewHolder extends MediaEntryViewHolder {


        public ViewHolder(View itemView) {
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
                                    A.EXTRA_RESULT_ALL,
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
                                    A.EXTRA_RESULT_COVER,
                                    getArtUrl(getAdapterPosition())
                            );
                            activity.setResult(Activity.RESULT_OK, returnIntent);
                            activity.finish();
                        }
                    }
            ).show(activity.getSupportFragmentManager(), "ONLINE");
        }
    }

    @NonNull
    protected abstract String getArtUrl(int position);
}
