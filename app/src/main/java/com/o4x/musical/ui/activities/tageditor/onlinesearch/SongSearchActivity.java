package com.o4x.musical.ui.activities.tageditor.onlinesearch;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.o4x.musical.R;
import com.o4x.musical.network.ApiClient;
import com.o4x.musical.network.CachingControlInterceptor;
import com.o4x.musical.network.service.ITunesService;
import com.o4x.musical.network.Models.ITunesModel;
import com.o4x.musical.ui.adapter.online.SongOnlineAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongSearchActivity
        extends AbsSearchOnlineActivity<SongOnlineAdapter, List<ITunesModel.Results>> {

    private static final String TAG = SongSearchActivity.class.getSimpleName();

    @NonNull
    @Override
    protected SongOnlineAdapter getAdapter() {
        return new SongOnlineAdapter(this, null);
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

        ApiClient.getClient(this).create(ITunesService.class)
                .searchITunes(songName, ITunesService.ENTITY_TRACK).enqueue(new Callback<ITunesModel>() {
            @Override
            public void onResponse(Call<ITunesModel> call, Response<ITunesModel> response) {
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
            public void onFailure(@NotNull Call<ITunesModel> call, @NotNull Throwable t) {
                Log.e(TAG, Objects.requireNonNull(t.getMessage()));
            }
        });
    }
}
