package com.o4x.musical.ui.activities.tageditor.onlinesearch;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.o4x.musical.R;
import com.o4x.musical.network.temp.Lastfmapi.ApiClient;
import com.o4x.musical.network.temp.Lastfmapi.LastFmInterface;
import com.o4x.musical.network.temp.Lastfmapi.Models.BestMatchesModel;
import com.o4x.musical.network.temp.Lastfmapi.CachingControlInterceptor;
import com.o4x.musical.ui.adapter.online.AlbumOnlineAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumSearchActivity
        extends AbsSearchOnlineActivity<AlbumOnlineAdapter, List<BestMatchesModel.Results>> {

    private static final String TAG = AlbumSearchActivity.class.getSimpleName();


    @NonNull
    @Override
    protected AlbumOnlineAdapter getAdapter() {
        return new AlbumOnlineAdapter(this, null);
    }

    @Override
    protected void fetchBestMatches(String songName) {
        progressBar.setVisibility(View.VISIBLE);
        resultsRecyclerView.setVisibility(View.INVISIBLE);
        empty.setVisibility(View.INVISIBLE);

        if (!CachingControlInterceptor.isOnline(getApplicationContext())) {
            Toast.makeText(progressBar.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getClient().create(LastFmInterface.class)
                .getITunesSong(ApiClient.ITUNES_API_URL, songName, "song").enqueue(new Callback<BestMatchesModel>() {
            @Override
            public void onResponse(Call<BestMatchesModel> call, Response<BestMatchesModel> response) {
                if (response.isSuccessful()) {
                    results = response.body().results;
                    onlineSearchAdapter.updateData(results);
                    progressBar.setVisibility(View.INVISIBLE);
                    if (results != null && results.size() == 0) {
                        empty.setVisibility(View.VISIBLE);
                        resultsRecyclerView.setVisibility(View.INVISIBLE);
                    } else {
                        empty.setVisibility(View.INVISIBLE);
                        resultsRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<BestMatchesModel> call, @NotNull Throwable t) {
                Log.e(TAG, Objects.requireNonNull(t.getMessage()));
            }
        });
    }
}