package github.o4x.musical.ui.activities.tageditor.onlinesearch;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import github.o4x.musical.R;
import github.o4x.musical.ui.activities.base.AbsMusicServiceActivity;
import github.o4x.musical.ui.adapter.online.SearchOnlineAdapter;
import github.o4x.musical.util.Util;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class AbsSearchOnlineActivity<A extends SearchOnlineAdapter, LR extends List<? extends Serializable>>
        extends AbsMusicServiceActivity {

    public static final int REQ_CODE_SPEECH_INPUT = 9003;

    public static final String QUERY = "query";
    public static final int REQUEST_CODE = 2000;
    public static final String EXTRA_RESULT_ALL = "EXTRA_RESULT_ALL";
    public static final String EXTRA_RESULT_COVER = "EXTRA_RESULT_COVER";
    public static final String EXTRA_SONG_NAME = "EXTRA_SONG_NAME";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.empty)
    TextView empty;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.results_recycler_view)
    RecyclerView resultsRecyclerView;
    @BindView(R.id.search_view)
    TextInputEditText searchView;
    @BindView(R.id.voice_search)
    AppCompatImageView voiceSearch;
    @BindView(R.id.clear_text)
    AppCompatImageView clearText;


    protected A onlineSearchAdapter;

    protected LR results;
    private String query = "";

    private Handler handler;
    private Runnable runnable;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_online);
        setDrawUnderBar();
        unbinder = ButterKnife.bind(this);

        setStatusBarColorAuto();
        setNavigationBarColorAuto();
        setTaskDescriptionColorAuto();
        setNavigationBarDividerColorAuto();

        setup();
        getExtra();
        if (savedInstanceState != null) {
            search(savedInstanceState.getString(QUERY));
        }

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                search(editable.toString());
            }
        });
        searchView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String mQuery = v.getText().toString();
                if (!query.equals(mQuery)) {
                    search(mQuery);
                }
                hideSoftKeyboard();
                return true;
            }
            return false;
        });
        searchView.setText(query);
        voiceSearch.setOnClickListener(v -> startMicSearch());
        clearText.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY, query);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getExtra() {
        search(getIntent().getStringExtra(EXTRA_SONG_NAME));
    }

    private void setup() {
        setupHandler();
        setUpToolBar();
        setupResultRecycler();
    }

    private void setUpToolBar() {
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

    private void startMicSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(
                    intent,
                    REQ_CODE_SPEECH_INPUT
            );
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    protected abstract A getAdapter();
    protected abstract void fetchBestMatches(String songName);
}
