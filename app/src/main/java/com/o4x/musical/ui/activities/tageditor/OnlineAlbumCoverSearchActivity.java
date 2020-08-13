package com.o4x.musical.ui.activities.tageditor;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.o4x.musical.R;
import com.o4x.musical.network.temp.Lastfmapi.ApiClient;
import com.o4x.musical.network.temp.Lastfmapi.LastFmInterface;
import com.o4x.musical.network.temp.Lastfmapi.Models.BestMatchesModel;
import com.o4x.musical.network.temp.Lastfmapi.CachingControlInterceptor;
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity;
import com.o4x.musical.ui.adapter.OnlineAlbumAdapter;
import com.o4x.musical.util.Util;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnlineAlbumCoverSearchActivity extends AbsMusicServiceActivity implements SearchView.OnQueryTextListener {

    public static final String QUERY = "query";
    public static int REQUEST_CODE = 2000;
    public static String EXTRA_RESULT_ALL = "EXTRA_RESULT_ALL";
    public static String EXTRA_RESULT_COVER = "EXTRA_RESULT_COVER";
    public static String EXTRA_SONG_NAME = "EXTRA_SONG_NAME";

    private static final String TAG = OnlineAlbumCoverSearchActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    SearchView searchView;
    @BindView(R.id.empty)
    TextView empty;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.results_recycler_view)
    public RecyclerView resultsRecyclerView;
    private OnlineAlbumAdapter onlineAlbumAdapter;

    private List<BestMatchesModel.Results> results;
    private String query = "";

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_album_cover);
        setDrawUnderBar();
        ButterKnife.bind(this);

        setStatusBarColorAuto();
        setNavigationBarColorAuto();
        setTaskDescriptionColorAuto();

        setup();
        getExtra();
        if (savedInstanceState != null) {
            search(savedInstanceState.getString(QUERY));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY, query);
    }

    private void getExtra() {
        search(getIntent().getStringExtra(EXTRA_SONG_NAME));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchItem.expandActionView();
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onBackPressed();
                return false;
            }
        });

        searchView.setQuery(query, false);
        searchView.post(() -> searchView.setOnQueryTextListener(OnlineAlbumCoverSearchActivity.this));

        return super.onCreateOptionsMenu(menu);
    }

    private void setup() {
        setupHandler();
        setUpToolBar();
        setupResultRecycler();
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupResultRecycler() {
        resultsRecyclerView.setLayoutManager(new GridLayoutManager(
                this,
                getResources().getInteger(R.integer.home_grid_columns)
        ));
        onlineAlbumAdapter = new OnlineAlbumAdapter(this, null);
        resultsRecyclerView.setAdapter(onlineAlbumAdapter);
        resultsRecyclerView.setOnTouchListener((v, event) -> {
            hideSoftKeyboard();
            return false;
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (this.query != query) {
            search(query);
        }
        hideSoftKeyboard();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        search(newText);
        return false;
    }

    private void setupHandler() {
        handler = new Handler();
        runnable = () -> fetchBestMatches(query.trim());
    }

    private void search(@NonNull String query) {
        this.query = query;
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 600);
    }

    private void hideSoftKeyboard() {
        Util.hideSoftKeyboard(OnlineAlbumCoverSearchActivity.this);
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    private void fetchBestMatches(String songName) {
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
                    onlineAlbumAdapter.updateData(results);
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