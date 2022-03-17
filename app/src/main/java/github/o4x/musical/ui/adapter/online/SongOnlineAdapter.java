package github.o4x.musical.ui.adapter.online;

import android.util.Log;

import androidx.annotation.NonNull;

import github.o4x.musical.imageloader.glide.loader.GlideLoader;
import github.o4x.musical.network.models.ITunesModel;
import github.o4x.musical.ui.activities.tageditor.onlinesearch.SongSearchActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class SongOnlineAdapter
        extends SearchOnlineAdapter<SongSearchActivity, List<ITunesModel.Results>> {

    private static final String TAG = SongOnlineAdapter.class.getSimpleName();

    public SongOnlineAdapter(SongSearchActivity activity,
                             List<ITunesModel.Results> resultsModels) {
        super(activity, resultsModels);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchOnlineAdapter.ViewHolder holder, int position) {
        try {
            String url = getArtUrl(position);
            if (holder.image != null)
                GlideLoader.with(holder.image.getContext())
                        .load(url, data.get(position).collectionName)
                        .into(holder.image);
            if (holder.title != null)
                holder.title.setText(data.get(position).trackName);
            if (holder.text != null)
                holder.text.setText(data.get(position).artistName);
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    @NotNull
    @Override
    protected String getArtUrl(int position) {
        return data.get(position).getBigArtworkUrl();
    }
}
