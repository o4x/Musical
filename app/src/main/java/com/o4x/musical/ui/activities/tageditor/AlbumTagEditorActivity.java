package com.o4x.musical.ui.activities.tageditor;

import android.app.Activity;
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

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.o4x.musical.R;
import com.o4x.musical.lastfm.rest.LastFMRestClient;
import com.o4x.musical.lastfm.rest.model.LastFmAlbum;
import com.o4x.musical.loader.AlbumLoader;
import com.o4x.musical.model.Song;
import com.o4x.musical.network.temp.Lastfmapi.Models.BestMatchesModel;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity;
import com.o4x.musical.util.ImageUtil;
import com.o4x.musical.util.LastFMUtil;
import com.o4x.musical.util.PhonographColorUtil;

import org.jaudiotagger.tag.FieldKey;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumTagEditorActivity extends AbsTagEditorActivity implements TextWatcher {

    private static final String TAG = AlbumTagEditorActivity.class.getSimpleName();

    @BindView(R.id.title)
    EditText albumTitle;
    @BindView(R.id.album_artist)
    EditText albumArtist;
    @BindView(R.id.genre)
    EditText genre;
    @BindView(R.id.year)
    EditText year;

    private Bitmap albumArtBitmap;
    private boolean deleteAlbumArt;
    private LastFMRestClient lastFMRestClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        lastFMRestClient = new LastFMRestClient(this);

        setUpViews();
    }

    private void setUpViews() {
        fillViewsWithFileTags();
        albumTitle.addTextChangedListener(this);
        albumArtist.addTextChangedListener(this);
        genre.addTextChangedListener(this);
        year.addTextChangedListener(this);
    }


    private void fillViewsWithFileTags() {
        albumTitle.setText(getAlbumTitle());
        albumArtist.setText(getAlbumArtistName());
        genre.setText(getGenreName());
        year.setText(getSongYear());
    }

    private void fillViewsWithResult(BestMatchesModel.Results result) {
        loadImageFromUrl(result.artworkUrl100);
        albumTitle.setText(result.collectionName);
        albumArtist.setText(result.artistName);
        genre.setText(result.primaryGenreName);
        year.setText(result.releaseDate);
    }

    @Override
    protected void loadCurrentImage() {
        Bitmap bitmap = getAlbumArt();
        setImageBitmap(bitmap, PhonographColorUtil.getColor(PhonographColorUtil.generatePalette(bitmap), ATHUtil.resolveColor(this, R.attr.defaultFooterColor)));
        deleteAlbumArt = false;
    }

    @Override
    protected void getImageFromLastFM() {
        String albumTitleStr = albumTitle.getText().toString();
        String albumArtistNameStr = albumArtist.getText().toString();
        if (albumArtistNameStr.trim().equals("") || albumTitleStr.trim().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.album_or_artist_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        lastFMRestClient.getApiService().getAlbumInfo(albumTitleStr, albumArtistNameStr, null).enqueue(new Callback<LastFmAlbum>() {
            @Override
            public void onResponse(Call<LastFmAlbum> call, Response<LastFmAlbum> response) {
                LastFmAlbum lastFmAlbum = response.body();
                if (lastFmAlbum.getAlbum() != null) {
                    String url = LastFMUtil.getLargestAlbumImageUrl(lastFmAlbum.getAlbum().getImage());
                    if (!TextUtils.isEmpty(url) && url.trim().length() > 0) {
                        loadImageFromUrl(url);
                        return;
                    }
                }
                toastLoadingFailed();
            }

            @Override
            public void onFailure(Call<LastFmAlbum> call, Throwable t) {
                toastLoadingFailed();
            }

            private void toastLoadingFailed() {
                Toast.makeText(AlbumTagEditorActivity.this,
                        R.string.could_not_download_album_cover, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void searchImageOnWeb() {
        searchWebFor(albumTitle.getText().toString(), albumArtist.getText().toString());
    }

    @Override
    protected void deleteImage() {
        setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art), ATHUtil.resolveColor(this, R.attr.defaultFooterColor));
        deleteAlbumArt = true;
        dataChanged();
    }

    @Override
    protected void searchOnline() {
        Intent intent = new Intent(this, AlbumSearchActivity.class);
        intent.putExtra(AlbumSearchActivity.EXTRA_SONG_NAME, getAlbumTitle());
        this.startActivityForResult(intent, AlbumSearchActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AlbumSearchActivity.REQUEST_CODE) {
            Bundle extras = data.getExtras();
            if (resultCode == Activity.RESULT_OK) {
                if (extras.containsKey(AlbumSearchActivity.EXTRA_RESULT_ALL)) {
                    BestMatchesModel.Results result = (BestMatchesModel.Results)
                            extras.getSerializable(AlbumSearchActivity.EXTRA_RESULT_ALL);
                    fillViewsWithResult(result);
                } else if (extras.containsKey(AlbumSearchActivity.EXTRA_RESULT_COVER)) {
                    loadImageFromUrl(
                            extras.getString(AlbumSearchActivity.EXTRA_RESULT_COVER)
                    );
                }
            } else {
                Log.i(TAG, "ResultCode = " + resultCode);
            }
        }
    }

    @Override
    protected void save() {
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        //android seems not to recognize album_artist field so we additionally write the normal artist field
        fieldKeyValueMap.put(FieldKey.ARTIST, albumArtist.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM_ARTIST, albumArtist.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genre.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());

        writeValuesToFiles(fieldKeyValueMap, deleteAlbumArt ? new ArtworkInfo(getId(), null) : albumArtBitmap == null ? null : new ArtworkInfo(getId(), albumArtBitmap));
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_album_tag_editor;
    }

    @NonNull
    @Override
    protected List<String> getSongPaths() {
        List<Song> songs = AlbumLoader.getAlbum(this, getId()).songs;
        List<String> paths = new ArrayList<>(songs.size());
        for (Song song : songs) {
            paths.add(song.data);
        }
        return paths;
    }

    @Override
    protected void loadImageFromFile(Uri selectedFile) {
        Glide.with(AlbumTagEditorActivity.this)
                .asBitmap()
                .load(selectedFile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        final Palette palette = Palette.from(resource).generate();
                        PhonographColorUtil.getColor(palette, Color.TRANSPARENT);
                        albumArtBitmap = ImageUtil.resizeBitmap(resource, 2048);
                        setImageBitmap(albumArtBitmap, PhonographColorUtil.getColor(palette, ATHUtil.resolveColor(AlbumTagEditorActivity.this, R.attr.defaultFooterColor)));
                        deleteAlbumArt = false;
                        dataChanged();
                        setResult(RESULT_OK);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
//                        e.printStackTrace();
//                        Toast.makeText(AlbumTagEditorActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void loadImageFromUrl(String url) {
        Glide.with(AlbumTagEditorActivity.this)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .error(R.drawable.default_album_art)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        albumArtBitmap = ImageUtil.resizeBitmap(resource, 2048);
                        setImageBitmap(albumArtBitmap, PhonographColorUtil.getColor(Palette.from(resource).generate(), ATHUtil.resolveColor(AlbumTagEditorActivity.this, R.attr.defaultFooterColor)));
                        deleteAlbumArt = false;
                        dataChanged();
                        setResult(RESULT_OK);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
//                                        e.printStackTrace();
//                                        Toast.makeText(AlbumTagEditorActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        dataChanged();
    }

    @Override
    protected void setColors(int color) {
        super.setColors(color);
        albumTitle.setTextColor(ToolbarContentTintHelper.toolbarTitleColor(this, color));
    }
}
