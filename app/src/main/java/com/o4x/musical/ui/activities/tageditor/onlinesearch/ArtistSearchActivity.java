package com.o4x.musical.ui.activities.tageditor.onlinesearch;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.o4x.musical.R;
import com.o4x.musical.network.ApiClient;
import com.o4x.musical.network.CachingControlInterceptor;
import com.o4x.musical.network.service.DeezerService;
import com.o4x.musical.network.models.DeezerArtistModel;
import com.o4x.musical.ui.adapter.online.ArtistOnlineAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistSearchActivity
        extends AbsSearchOnlineActivity<ArtistOnlineAdapter, List<DeezerArtistModel.Data>> {

    private static final String TAG = ArtistSearchActivity.class.getSimpleName();

    @NonNull
    @Override
    protected ArtistOnlineAdapter getAdapter() {
        return new ArtistOnlineAdapter(this, null);
    }

    @Override
    protected void fetchBestMatches(String artistName) {
        progressBar.setVisibility(View.VISIBLE);
        resultsRecyclerView.setVisibility(View.INVISIBLE);
        empty.setVisibility(View.INVISIBLE);

        if (!CachingControlInterceptor.isOnline(getApplicationContext())) {
            Toast.makeText(progressBar.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getClient(this).create(DeezerService.class)
                .searchDeezerArtist(artistName).enqueue(new Callback<DeezerArtistModel>() {
            @Override
            public void onResponse(Call<DeezerArtistModel> call, Response<DeezerArtistModel> response) {
                if (response.isSuccessful()) {
                    results = response.body().data;
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
            public void onFailure(@NotNull Call<DeezerArtistModel> call, @NotNull Throwable t) {
                Log.e(TAG, Objects.requireNonNull(t.getMessage()));
            }
        });
    }

}
