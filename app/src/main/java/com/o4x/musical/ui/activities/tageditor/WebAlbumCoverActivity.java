package com.o4x.musical.ui.activities.tageditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.o4x.musical.R;
import com.o4x.musical.network.temp.Lastfmapi.ApiClient;
import com.o4x.musical.network.temp.Lastfmapi.LastFmInterface;
import com.o4x.musical.network.temp.Lastfmapi.Models.BestMatchesModel;
import com.o4x.musical.model.Song;
import com.o4x.musical.network.temp.Lastfmapi.CachingControlInterceptor;
import com.o4x.musical.ui.adapter.BestMatchesAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebAlbumCoverActivity extends AppCompatActivity {

    @BindView(R.id.edit_text_search)
    EditText mBestMatchesEdiText;
    @BindView(R.id.text_view_no_matches_found)
    TextView mNoMatchesFoundTextView;

    @BindView(R.id.best_matches_recycler_view)
    public RecyclerView mBestMatchesRecyclerView;
    private BestMatchesAdapter mBestMatchesAdapter;
    private List<BestMatchesModel.Results> results;
    private String SONG_NAME = "1000";
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            fetchBestMatches(mBestMatchesEdiText.getText().toString().trim());
        }
    };
    @BindView(R.id.image_button_cross)
    ImageView mCrossImageViewButton;
    @BindView(R.id.image_back_button)
    ImageView mBackImageViewButton;
    private Handler mHandler;

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().length() == 0) {
                mCrossImageViewButton.setVisibility(View.INVISIBLE);
            } else {
                mCrossImageViewButton.setVisibility(View.VISIBLE);
            }
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 600);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_album_cover);
        ButterKnife.bind(this);

        mHandler = new Handler();

        mCrossImageViewButton.setOnClickListener(v -> mBestMatchesEdiText.setText(""));


        if (SONG_NAME.length() > 0) {
            mCrossImageViewButton.setVisibility(View.VISIBLE);
        }

        mBestMatchesEdiText.setText(SONG_NAME);
        mBestMatchesEdiText.setSelection(SONG_NAME.length());
        mBestMatchesEdiText.addTextChangedListener(mTextWatcher);

        mBestMatchesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mBestMatchesAdapter = new BestMatchesAdapter(this, null);
        mBestMatchesRecyclerView.setAdapter(mBestMatchesAdapter);

        fetchBestMatches(SONG_NAME);
    }

    private void fetchBestMatches(String songName) {
        mProgressBar.setVisibility(View.VISIBLE);
        mBestMatchesRecyclerView.setVisibility(View.INVISIBLE);
        mNoMatchesFoundTextView.setVisibility(View.INVISIBLE);

        if (!CachingControlInterceptor.isOnline(getApplicationContext())) {
            Toast.makeText(mProgressBar.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getClient().create(LastFmInterface.class)
                .getITunesSong(ApiClient.ITUNES_API_URL, songName, "song").enqueue(new Callback<BestMatchesModel>() {
            @Override
            public void onResponse(Call<BestMatchesModel> call, Response<BestMatchesModel> response) {
                if (response.isSuccessful()) {
                    results = response.body().results;
                    mBestMatchesAdapter.updateData(results);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (results != null && results.size() == 0) {
                        mNoMatchesFoundTextView.setVisibility(View.VISIBLE);
                        mBestMatchesRecyclerView.setVisibility(View.INVISIBLE);
                    } else {
                        mNoMatchesFoundTextView.setVisibility(View.INVISIBLE);
                        mBestMatchesRecyclerView.setVisibility(View.VISIBLE);
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<BestMatchesModel> call, Throwable t) {
                Log.e("FIALED", t.getMessage());
            }
        });
    }


    public void updateAlbumArt(String url) {
        Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
    }
}