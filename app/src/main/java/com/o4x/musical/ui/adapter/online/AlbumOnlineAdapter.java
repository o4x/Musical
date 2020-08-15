package com.o4x.musical.ui.adapter.online;

import android.util.Log;

import androidx.annotation.NonNull;

import com.o4x.musical.imageloader.universalil.UniversalIL;
import com.o4x.musical.network.temp.Lastfmapi.Models.ITunesResultModel;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlbumOnlineAdapter
        extends SongOnlineAdapter {

    public AlbumOnlineAdapter(AlbumSearchActivity activity,
                              List<ITunesResultModel.Results> resultsModels) {
        super(activity, resultsModels);
    }
}
