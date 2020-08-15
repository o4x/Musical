package com.o4x.musical.ui.adapter.online;

import android.util.Log;

import androidx.annotation.NonNull;

import com.o4x.musical.imageloader.universalil.UniversalIL;
import com.o4x.musical.network.temp.Lastfmapi.Models.BestMatchesModel;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlbumOnlineAdapter
        extends SearchOnlineAdapter<AlbumSearchActivity, List<BestMatchesModel.Results>> {

    private static final String TAG = AlbumOnlineAdapter.class.getSimpleName();

    public AlbumOnlineAdapter(AlbumSearchActivity activity,
                              List<BestMatchesModel.Results> resultsModels) {
        super(activity, resultsModels);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchOnlineAdapter.ViewHolder holder, int position) {
        try {
            String url = getArtUrl(position);
            assert holder.image != null;
            UniversalIL.onlineAlbumImageLoader(url, holder.image, null);
            assert holder.title != null;
            holder.title.setText(data.get(position).trackName);
            assert holder.text != null;
            holder.text.setText(data.get(position).artistName);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @NotNull
    @Override
    protected String getArtUrl(int position) {
        return data.get(position).artworkUrl100
                .replace("100x100", "500x500");
    }
}
