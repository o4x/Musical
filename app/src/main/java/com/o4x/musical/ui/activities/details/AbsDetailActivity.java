package com.o4x.musical.ui.activities.details;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.loader.app.LoaderManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.google.android.material.textview.MaterialTextView;
import com.o4x.musical.R;
import com.o4x.musical.imageloader.universalil.listener.PaletteMusicLoadingListener;
import com.o4x.musical.imageloader.universalil.loader.UniversalIL;
import com.o4x.musical.interfaces.CabHolder;
import com.o4x.musical.interfaces.PaletteColorHolder;
import com.o4x.musical.model.Song;
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity;
import com.o4x.musical.ui.adapter.song.DetailsSongAdapter;
import com.o4x.musical.util.PhonographColorUtil;
import com.o4x.musical.util.Util;
import com.o4x.musical.util.color.MediaNotificationProcessor;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.appthemehelper.util.ColorUtil;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;

public abstract class AbsDetailActivity<T> extends AbsMusicPanelActivity implements PaletteColorHolder, CabHolder, LoaderManager.LoaderCallbacks<T> {

    static final int TAG_EDITOR_REQUEST = 2001;

    @BindView(R.id.nested_scroll_view)
    NestedScrollView scrollView;
    @BindView(R.id.song_recycler)
    RecyclerView songRecyclerView;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.header)
    View headerView;
    @BindView(R.id.gradient)
    View gradient;
    @BindView(R.id.title)
    MaterialTextView title;
    @BindView(R.id.subtitle)
    MaterialTextView subtitle;

    MaterialCab cab;
    int imageHeight;
    MediaNotificationProcessor colors;

    @Nullable
    Spanned wiki;
    MaterialDialog wikiDialog;

    DetailsSongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setDrawUnderStatusBar();
        setUpToolBar();
        setupViews();

        reload();
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_detail);
    }

    @Override
    public int getPaletteColor() {
        return colors.getActionBarColor();
    }

    @NonNull
    @Override
    public MaterialCab openCab(int menuRes, @NonNull final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(PhonographColorUtil.shiftBackgroundColorForLightText(getPaletteColor()))
                .start(new MaterialCab.Callback() {
                    @Override
                    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
                        return callback.onCabCreated(materialCab, menu);
                    }

                    @Override
                    public boolean onCabItemClicked(MenuItem menuItem) {
                        return callback.onCabItemClicked(menuItem);
                    }

                    @Override
                    public boolean onCabFinished(MaterialCab materialCab) {
                        return callback.onCabFinished(materialCab);
                    }
                });
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else {
            scrollView.stopNestedScroll();
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAG_EDITOR_REQUEST) {
            reload();
            setResult(RESULT_OK);
        }
    }

    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void setupViews() {
        setupScrollView();
        setupSongsRecycler();
        setColors(DialogUtils.resolveColor(this, R.attr.defaultFooterColor), null);
    }

    void setupScrollView() {
        final int displayHeight = Util.getScreenHeight();
        imageHeight = Util.getScreenWidth();
        final int gradientHeight =
                (int) (imageHeight + getResources().getDimension(R.dimen.detail_header_height));

        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                // Change alpha of overlay
                final float alpha = Math.max(0, Math.min(1, (float) 2 * scrollY / gradientHeight));
                setAppbarAlpha(alpha);

                // Scroll poster
                image.setTranslationY(
                        Math.max(-scrollY / (displayHeight * 2 / imageHeight), -imageHeight)
                );
            }
        });
    }

    void setupSongsRecycler() {
        songAdapter = new DetailsSongAdapter(this, getSongs(), R.layout.item_list, false, this);
        songRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        songRecyclerView.setAdapter(songAdapter);
        songAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (songAdapter.getItemCount() == 0) finish();
            }
        });
    }

    private void setColors(int color, @Nullable MediaNotificationProcessor colors) {
        if (colors != null) {
            this.colors = colors;
            ToolbarContentTintHelper.colorizeToolbar(toolbar, colors.getPrimaryTextColor(), this);
            setNavigationBarColor(colors.getActionBarColor());
            setTaskDescriptionColor(colors.getActionBarColor());

            title.setTextColor(colors.getPrimaryTextColor());
            subtitle.setTextColor(colors.getSecondaryTextColor());
        }

        gradient.setBackgroundTintList(ColorStateList.valueOf(color));
        headerView.setBackgroundColor(color);
        findViewById(android.R.id.content).getRootView().setBackgroundColor(color);

        setAppbarAlpha(0);
    }

    private void setAppbarAlpha(float alpha) {
        if (colors == null) return;
        toolbar.setBackgroundColor(
                ColorUtil.INSTANCE.withAlpha(colors.getActionBarColor(), alpha)
        );
        setStatusBarColor(
                ColorUtil.INSTANCE.withAlpha(colors.getActionBarColor(), alpha)
        );
        toolbar.setTitleTextColor(
                ColorUtil.INSTANCE.withAlpha(colors.getPrimaryTextColor(), alpha)
        );
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        reload();
    }

    UniversalIL getImageLoader() {
        return new UniversalIL(
                image,
                new PaletteMusicLoadingListener() {
                    @Override
                    public void onColorReady(@NotNull MediaNotificationProcessor colors) {
                        setColors(colors.getBackgroundColor(), colors);
                        setMiniPlayerColor(colors);
                        songAdapter.setColors(colors);
                    }
                }, imageHeight
        );
    }

    abstract void loadImage();
    abstract void reload();
    protected abstract List<Song> getSongs();
}
