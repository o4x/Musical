package com.o4x.musical.ui.activities.tageditor.onlinesearch;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.o4x.musical.R;
import com.o4x.musical.network.temp.Lastfmapi.ApiClient;
import com.o4x.musical.network.temp.Lastfmapi.LastFmInterface;
import com.o4x.musical.network.temp.Lastfmapi.Models.ITunesResultModel;
import com.o4x.musical.network.temp.Lastfmapi.CachingControlInterceptor;
import com.o4x.musical.ui.adapter.online.AlbumOnlineAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumSearchActivity
        extends SongSearchActivity {

    @NonNull
    @Override
    protected AlbumOnlineAdapter getAdapter() {
        return new AlbumOnlineAdapter(this, null);
    }
}