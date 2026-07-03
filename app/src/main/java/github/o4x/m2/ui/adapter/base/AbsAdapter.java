package github.o4x.m2.ui.adapter.base;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import github.o4x.m2.R;
import github.o4x.m2.helper.MyPalette;
import github.o4x.m2.helper.menu.SongsMenuHelper;
import github.o4x.m2.imageloader.glide.loader.GlideLoader;
import github.o4x.m2.imageloader.glide.targets.palette.PaletteTargetListener;
import github.o4x.m2.model.Song;
import github.o4x.m2.prefs.PreferenceUtil;
import github.o4x.m2.util.ColorExtKt;
import github.o4x.m2.util.ColorUtil;
import github.o4x.m2.util.MaterialValueHelper;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public abstract class AbsAdapter<VH extends MediaEntryViewHolder, I>
        extends AbsMultiSelectAdapter<VH, I> implements FastScrollRecyclerView.SectionedAdapter {

    @NonNull
    protected final AppCompatActivity activity;
    protected List<I> dataSet;
    protected int itemLayoutRes;
    @Nullable
    protected RecyclerView recyclerView;
    protected boolean coloredFooter;

    public AbsAdapter(@NonNull AppCompatActivity activity, List<I> dataSet, @LayoutRes int itemLayoutRes, @MenuRes int menu) {
        super(activity, menu);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.coloredFooter = PreferenceUtil.isColoredFooter();
    }

    public AbsAdapter(@NonNull AppCompatActivity activity, List<I> dataSet, @LayoutRes int itemLayoutRes) {
        super(activity, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.coloredFooter = PreferenceUtil.isColoredFooter();
    }

    public List<I> getDataSet() {
        return dataSet;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public void swapDataSet(List<I> dataSet) {
        this.dataSet = dataSet;
        // End any running animations before notifying to prevent a crash when an
        // add-animation is still in flight as the dataset is replaced.
        if (recyclerView != null && recyclerView.getItemAnimator() != null) {
            recyclerView.getItemAnimator().endAnimations();
        }
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view, viewType);
    }

    protected abstract VH createViewHolder(@NonNull View view, int viewType);

    @Override
    public void onViewRecycled(@NonNull VH holder) {
        super.onViewRecycled(holder);
        setColors(ColorExtKt.cardColor(activity), holder);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final I data = dataSet.get(position);
        loadImage(data, holder);
    }

    protected abstract void loadImage(I data, final VH holder);

    protected GlideLoader.GlideBuilder getImageLoader(@NonNull VH holder) {
        return GlideLoader.with(activity)
                .withListener(coloredFooter ?
                        new PaletteTargetListener(activity) {
                            @Override
                            public void onColorReady(@NotNull MyPalette colors, @Nullable Bitmap resource) {
                                setColors(colors.getBackgroundColor(), holder);
                            }
                        } : null);
    }

    protected void setColors(int color, VH holder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            if (coloredFooter) {
                if (holder.title != null) {
                    holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.INSTANCE.isColorLight(color)));
                }
                if (holder.text != null) {
                    holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.INSTANCE.isColorLight(color)));
                }
                if (holder.menu != null) {
                    holder.menu.setColorFilter(
                            MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.INSTANCE.isColorLight(color)),
                            PorterDuff.Mode.SRC_IN
                    );
                }
            } else {
                if (holder.title != null) {
                    holder.title.setTextColor(ColorExtKt.textColorPrimary(activity));
                }
                if (holder.text != null) {
                    holder.text.setTextColor(ColorExtKt.textColorSecondary(activity));
                }
                if (holder.menu != null) {
                    holder.menu.setColorFilter(
                            ColorExtKt.textColorSecondary(activity),
                            PorterDuff.Mode.SRC_IN
                    );
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected I getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull List<I> selection) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
    }

    @NonNull
    protected abstract List<Song> getSongList(@NonNull List<I> data);

}
