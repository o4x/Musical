package com.o4x.musical.ui.activities.tageditor;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.palette.graphics.Palette;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.o4x.musical.R;
import com.o4x.musical.ui.activities.base.AbsBaseActivity;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AbsSearchOnlineActivity;
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity;
import com.o4x.musical.util.ImageUtil;
import com.o4x.musical.util.PhonographColorUtil;
import com.o4x.musical.util.TagUtil;

import org.jaudiotagger.tag.FieldKey;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsTagEditorActivity<RM extends Serializable> extends AbsBaseActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_PALETTE = "extra_palette";
    private static final String TAG = AbsTagEditorActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_IMAGE = 1000;


    @BindView(R.id.play_pause_fab)
    FloatingActionButton fab;
    @BindView(R.id.search_online_btn)
    AppCompatButton searchBtn;
    @BindView(R.id.nested_scroll_view)
    NestedScrollView scrollView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.header)
    LinearLayout header;

    @Nullable
    @BindView(R.id.song_name)
    EditText songName;
    @Nullable
    @BindView(R.id.album_name)
    EditText albumName;
    @Nullable
    @BindView(R.id.artist_name)
    EditText artistName;
    @Nullable
    @BindView(R.id.genre_name)
    EditText genreName;
    @Nullable
    @BindView(R.id.year)
    EditText year;
    @Nullable
    @BindView(R.id.track_number)
    EditText trackNumber;
    @Nullable
    @BindView(R.id.lyrics)
    EditText lyrics;

    private int id;
    private int headerVariableSpace;
    private int paletteColorPrimary;
    protected TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            dataChanged();
        }
    };

    private Bitmap albumArtBitmap;
    private boolean deleteAlbumArt;
    protected TagUtil tagUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayout());
        ButterKnife.bind(this);

        getIntentExtras();

        createTagUtil();

        headerVariableSpace = getResources().getDimensionPixelSize(R.dimen.tagEditorHeaderVariableSpace);

        setupViews();

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.action_tag_editor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            id = intentExtras.getInt(EXTRA_ID);
        }
    }

    private void createTagUtil() {
        List<String> songPaths = getSongPaths();
        if (songPaths.isEmpty()) {
            finish();
            return;
        }
        tagUtil = new TagUtil(this, songPaths);
    }

    private void setupViews() {
        setupScrollView();
        setupColors();
        setupFab();
        setupSearchButton();
        setupImageView();
        setupEditTexts();
    }

    private void setupScrollView() {
        scrollView.setOnScrollChangeListener(
                new NestedScrollView.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        image.setTranslationY(scrollY / 2f);
                    }
                }
        );
    }

    private void setupFab() {
        fab.setScaleX(0);
        fab.setScaleY(0);
        fab.setEnabled(false);
        fab.setOnClickListener(v -> save());

        TintHelper.setTintAuto(fab, ThemeStore.accentColor(this), true);
    }

    private void setupSearchButton() {
        searchBtn.setBackgroundColor(ThemeStore.primaryColor(this));
        searchBtn.setOnClickListener(view -> searchOnline());
    }

    private void setupImageView() {
        loadCurrentImage();
        final CharSequence[] items = new CharSequence[]{
                getString(R.string.download_from_last_fm),
                getString(R.string.pick_from_local_storage),
                getString(R.string.web_search),
                getString(R.string.remove_cover)
        };
        image.setOnClickListener(v -> new MaterialDialog.Builder(AbsTagEditorActivity.this)
                .title(R.string.update_image)
                .items(items)
                .itemsCallback((dialog, view, which, text) -> {
                    switch (which) {
                        case 0:
                            getImageFromLastFM();
                            break;
                        case 1:
                            startImagePicker();
                            break;
                        case 2:
                            searchImageOnWeb();
                            break;
                        case 3:
                            deleteImage();
                            break;
                    }
                }).show());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupEditTexts() {
        fillViewsWithFileTags();
        if (songName != null)
        songName.addTextChangedListener(textWatcher);
        if (albumName != null)
        albumName.addTextChangedListener(textWatcher);
        if (artistName != null)
        artistName.addTextChangedListener(textWatcher);
        if (genreName != null)
        genreName.addTextChangedListener(textWatcher);
        if (year != null)
        year.addTextChangedListener(textWatcher);
        if (trackNumber != null)
        trackNumber.addTextChangedListener(textWatcher);
        if (lyrics != null)
        lyrics.addTextChangedListener(textWatcher);
    }

    private void fillViewsWithFileTags() {
        if (songName != null)
        songName.setText(tagUtil.getSongTitle());
        if (albumName != null)
        albumName.setText(tagUtil.getAlbumTitle());
        if (artistName != null)
        artistName.setText(tagUtil.getArtistName());
        if (genreName != null)
        genreName.setText(tagUtil.getGenreName());
        if (year != null)
        year.setText(tagUtil.getSongYear());
        if (trackNumber != null)
        trackNumber.setText(tagUtil.getTrackNumber());
        if (lyrics != null)
        lyrics.setText(tagUtil.getLyrics());
    }

    protected abstract void fillViewsWithResult(RM result);

    protected void dataChanged() {
        showFab();
    }

    private void showFab() {
        fab.animate()
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .scaleX(1)
                .scaleY(1)
                .start();
        fab.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    loadImageFromFile(selectedImage);
                }
                break;
            case AbsSearchOnlineActivity.REQUEST_CODE:
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            if (extras.containsKey(AlbumSearchActivity.EXTRA_RESULT_ALL)) {
                                RM result = (RM)
                                        extras.getSerializable(AbsSearchOnlineActivity.EXTRA_RESULT_ALL);
                                if (result != null)
                                fillViewsWithResult(result);
                            } else if (extras.containsKey(AbsSearchOnlineActivity.EXTRA_RESULT_COVER)) {
                                loadImageFromUrl(
                                        extras.getString(AbsSearchOnlineActivity.EXTRA_RESULT_COVER)
                                );
                            }
                        }
                    } else {
                        Log.i(TAG, "ResultCode = " + resultCode);
                    }
                } catch (Exception e) {
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                }
                break;
            default:
                break;
        }
    }

    protected void setImageBitmap(@Nullable final Bitmap bitmap, int bgColor) {
        if (bitmap == null) {
            image.setImageResource(R.drawable.default_album_art);
        } else {
            image.setImageBitmap(bitmap);
        }
    }

    private void setupColors() {
        paletteColorPrimary = ThemeStore.primaryColor(this);
        header.setBackgroundColor(paletteColorPrimary);
        toolbar.setBackgroundColor(paletteColorPrimary);
        setStatusBarColor(paletteColorPrimary);
        setNavigationBarColor(paletteColorPrimary);
        setTaskDescriptionColor(paletteColorPrimary);
    }

    protected int getId() {
        return id;
    }

    protected void searchWebFor(String... keys) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keys) {
            stringBuilder.append(key);
            stringBuilder.append(" ");
        }
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, stringBuilder.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    private void startImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_from_local_storage)), REQUEST_CODE_SELECT_IMAGE);
    }


    protected void loadCurrentImage() {
        Bitmap bitmap = tagUtil.getAlbumArt();
        setImageBitmap(bitmap, PhonographColorUtil.getColor(PhonographColorUtil.generatePalette(bitmap), ATHUtil.resolveColor(this, R.attr.defaultFooterColor)));
        deleteAlbumArt = false;
    }




    protected void deleteImage() {
        setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art), ATHUtil.resolveColor(this, R.attr.defaultFooterColor));
        deleteAlbumArt = true;
        dataChanged();
    }


    protected void save() {
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        if (songName != null)
        fieldKeyValueMap.put(FieldKey.TITLE, songName.getText().toString());
        if (albumName != null)
        fieldKeyValueMap.put(FieldKey.ALBUM, albumName.getText().toString());
        if (artistName != null)
        fieldKeyValueMap.put(FieldKey.ARTIST, artistName.getText().toString());
        if (genreName != null)
        fieldKeyValueMap.put(FieldKey.GENRE, genreName.getText().toString());
        if (year != null)
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());
        if (trackNumber != null)
        fieldKeyValueMap.put(FieldKey.TRACK, trackNumber.getText().toString());
        if (lyrics != null)
        fieldKeyValueMap.put(FieldKey.LYRICS, lyrics.getText().toString());
        tagUtil.writeValuesToFiles(fieldKeyValueMap, deleteAlbumArt ? new TagUtil.ArtworkInfo(getId(), null) : albumArtBitmap == null ? null : new TagUtil.ArtworkInfo(getId(), albumArtBitmap));
    }


    protected void loadImageFromFile(Uri selectedFile) {
        Glide.with(AbsTagEditorActivity.this)
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
                        setImageBitmap(albumArtBitmap, PhonographColorUtil.getColor(palette, ATHUtil.resolveColor(AbsTagEditorActivity.this, R.attr.defaultFooterColor)));
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
                    }
                });
    }


    protected void loadImageFromUrl(String url) {
        Glide.with(AbsTagEditorActivity.this)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .error(R.drawable.default_album_art)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        albumArtBitmap = ImageUtil.resizeBitmap(resource, 2048);
                        setImageBitmap(albumArtBitmap, PhonographColorUtil.getColor(Palette.from(resource).generate(), ATHUtil.resolveColor(AbsTagEditorActivity.this, R.attr.defaultFooterColor)));
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
                    }
                });
    }


    protected abstract int getContentViewLayout();

    @NonNull
    protected abstract List<String> getSongPaths();

    protected abstract void getImageFromLastFM();

    protected abstract void searchImageOnWeb();

    protected abstract void searchOnline();
}
