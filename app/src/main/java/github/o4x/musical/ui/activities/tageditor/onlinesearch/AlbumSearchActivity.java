package github.o4x.musical.ui.activities.tageditor.onlinesearch;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import github.o4x.musical.R;
import github.o4x.musical.network.ApiClient;
import github.o4x.musical.network.CachingControlInterceptor;
import github.o4x.musical.network.service.ITunesService;
import github.o4x.musical.network.models.ITunesModel;
import github.o4x.musical.ui.adapter.online.AlbumOnlineAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumSearchActivity
        extends AbsSearchOnlineActivity<AlbumOnlineAdapter, List<ITunesModel.Results>> {

    private static final String TAG = AlbumSearchActivity.class.getSimpleName();

    @NonNull
    @Override
    protected AlbumOnlineAdapter getAdapter() {
        return new AlbumOnlineAdapter(this, null);
    }

    @Override
    protected void fetchBestMatches(String albumName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.resultsRecyclerView.setVisibility(View.INVISIBLE);
        binding.empty.setVisibility(View.INVISIBLE);

        if (!CachingControlInterceptor.isOnline(getApplicationContext())) {
            Toast.makeText(binding.progressBar.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getClient(this).create(ITunesService.class)
                .searchITunes(albumName, ITunesService.ENTITY_ALBUM).enqueue(new Callback<ITunesModel>() {
            @Override
            public void onResponse(Call<ITunesModel> call, Response<ITunesModel> response) {
                if (response.isSuccessful()) {
                    results = response.body().results;
                    onlineSearchAdapter.updateData(results);
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    if (results != null && results.size() == 0) {
                        binding.empty.setVisibility(View.VISIBLE);
                        binding.resultsRecyclerView.setVisibility(View.INVISIBLE);
                    } else {
                        binding.empty.setVisibility(View.INVISIBLE);
                        binding.resultsRecyclerView.setVisibility(View.VISIBLE);
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