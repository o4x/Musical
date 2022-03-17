package github.o4x.musical.ui.adapter.online;

import android.util.Log;

import androidx.annotation.NonNull;

import github.o4x.musical.imageloader.glide.loader.GlideLoader;
import github.o4x.musical.network.models.DeezerArtistModel;
import github.o4x.musical.ui.activities.tageditor.onlinesearch.ArtistSearchActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ArtistOnlineAdapter
        extends SearchOnlineAdapter<ArtistSearchActivity, List<DeezerArtistModel.Data>> {

    private static final String TAG = ArtistOnlineAdapter.class.getSimpleName();

    public ArtistOnlineAdapter(ArtistSearchActivity activity,
                              List<DeezerArtistModel.Data> resultsModels) {
        super(activity, resultsModels);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchOnlineAdapter.ViewHolder holder, int position) {
        try {
            String url = getArtUrl(position);
            if (holder.image != null)
                GlideLoader.with(holder.image.getContext())
                        .load(url, data.get(position).name)
                        .into(holder.image);
            if (holder.title != null)
                holder.title.setText(data.get(position).name);
            if (holder.text != null)
                holder.text.setText(String.valueOf(data.get(position).id));
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    @NotNull
    @Override
    protected String getArtUrl(int position) {
        return data.get(position).pictureXl;
    }

}
