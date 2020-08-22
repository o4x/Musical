package com.o4x.musical.ui.adapter.online;

import android.util.Log;

import androidx.annotation.NonNull;

import com.o4x.musical.imageloader.universalil.UniversalIL;
import com.o4x.musical.network.Models.ITunesModel;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.SongSearchActivity;

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
            UniversalIL.onlineAlbumImageLoader(url, holder.image, null);
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
