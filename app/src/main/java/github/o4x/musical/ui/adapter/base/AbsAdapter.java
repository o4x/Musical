package github.o4x.musical.ui.adapter.base;

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

import github.o4x.musical.R;
import github.o4x.musical.helper.MyPalette;
import github.o4x.musical.helper.menu.SongsMenuHelper;
import github.o4x.musical.imageloader.glide.loader.GlideLoader;
import github.o4x.musical.imageloader.glide.targets.palette.PaletteTargetListener;
import github.o4x.musical.interfaces.CabHolder;
import github.o4x.musical.model.Song;
import github.o4x.musical.prefs.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import com.o4x.appthemehelper.extensions.ColorExtKt;
import com.o4x.appthemehelper.util.ColorUtil;
import com.o4x.appthemehelper.util.MaterialValueHelper;

public abstract class AbsAdapter<VH extends MediaEntryViewHolder, I>
        extends AbsMultiSelectAdapter<VH, I> implements FastScrollRecyclerView.SectionedAdapter {

    @NonNull
    protected final AppCompatActivity activity;
    protected List<I> dataSet;
    protected int itemLayoutRes;

    public AbsAdapter(@NonNull AppCompatActivity activity, List<I> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder, @MenuRes int menu) {
        super(activity, cabHolder, menu);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
    }

    public AbsAdapter(@NonNull AppCompatActivity activity, List<I> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
    }

    public List<I> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(List<I> dataSet) {
        this.dataSet = dataSet;
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

        boolean isChecked = isChecked(data);
        holder.itemView.setActivated(isChecked);
        loadImage(data, holder);
    }

    protected abstract void loadImage(I data, final VH holder);

    protected GlideLoader.GlideBuilder getImageLoader(@NonNull VH holder) {
        return GlideLoader.with(activity)
                .withListener(PreferenceUtil.isColoredFooter() ?
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
            if (PreferenceUtil.isColoredFooter()) {
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
