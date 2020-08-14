package com.o4x.musical.ui.activities.tageditor.onlinesearch;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.o4x.musical.R;
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity;
import com.o4x.musical.ui.adapter.online.OnlineSearchAdapter;
import com.o4x.musical.util.Util;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class AbsOnlineSearch <A extends OnlineSearchAdapter, LR extends List<? extends Serializable>>
        extends AbsMusicServiceActivity implements SearchView.OnQueryTextListener {

    public static final String QUERY = "query";
    public static int REQUEST_CODE = 2000;
    public static String EXTRA_RESULT_ALL = "EXTRA_RESULT_ALL";
    public static String EXTRA_RESULT_COVER = "EXTRA_RESULT_COVER";
    public static String EXTRA_SONG_NAME = "EXTRA_SONG_NAME";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    SearchView searchView;
    @BindView(R.id.empty)
    TextView empty;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.results_recycler_view)
    RecyclerView resultsRecyclerView;
    protected A onlineSearchAdapter;

    protected LR results;
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
        searchView.post(() -> searchView.setOnQueryTextListener(this));

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
        onlineSearchAdapter = getAdapter();
        resultsRecyclerView.setAdapter(onlineSearchAdapter);
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
        Util.hideSoftKeyboard(this);
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    @NonNull
    protected abstract A getAdapter();
    protected abstract void fetchBestMatches(String songName);
}
