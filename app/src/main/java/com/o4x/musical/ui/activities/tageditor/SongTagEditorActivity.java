package com.o4x.musical.ui.activities.tageditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.o4x.musical.R;
import com.o4x.musical.loader.SongLoader;
import com.o4x.musical.network.temp.Lastfmapi.Models.ITunesResultModel;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.SongSearchActivity;
import com.o4x.musical.util.ImageUtil;
import com.o4x.musical.util.PhonographColorUtil;
import com.o4x.musical.util.TagUtil;

import org.jaudiotagger.tag.FieldKey;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class SongTagEditorActivity extends AbsTagEditorActivity {


    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_song_tag_editor;
    }

    @NonNull
    @Override
    protected List<String> getSongPaths() {
        List<String> paths = new ArrayList<>(1);
        paths.add(SongLoader.getSong(this, getId()).data);
        return paths;
    }

    @Override
    protected void setColors(int color) {
        super.setColors(color);
        int toolbarTitleColor = ToolbarContentTintHelper.toolbarTitleColor(this, color);
        songName.setTextColor(toolbarTitleColor);
        albumName.setTextColor(toolbarTitleColor);
    }

    @Override
    protected void searchImageOnWeb() {
        searchWebFor(songName.getText().toString(), artistName.getText().toString());
    }

    @Override
    protected void searchOnline() {
        Intent intent = new Intent(this, SongSearchActivity.class);
        intent.putExtra(SongSearchActivity.EXTRA_SONG_NAME, tagUtil.getSongTitle());
        this.startActivityForResult(intent, SongSearchActivity.REQUEST_CODE);
    }

    @Override
    protected void getImageFromLastFM() {

    }

}
