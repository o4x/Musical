package github.o4x.musical.ui.adapter;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import github.o4x.musical.R;
import github.o4x.musical.imageloader.glide.loader.GlideLoader;
import github.o4x.musical.imageloader.model.AudioFileCover;
import github.o4x.musical.ui.adapter.base.AbsMultiSelectAdapter;
import github.o4x.musical.ui.adapter.base.MediaEntryViewHolder;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;


public class SongFileAdapter extends AbsMultiSelectAdapter<SongFileAdapter.ViewHolder, File> implements FastScrollRecyclerView.SectionedAdapter {

    private static final int FILE = 0;
    private static final int FOLDER = 1;

    private final AppCompatActivity activity;
    private List<File> dataSet;
    private final int itemLayoutRes;
    @Nullable
    private final Callbacks callbacks;

    public SongFileAdapter(@NonNull AppCompatActivity activity, @NonNull List<File> songFiles, @LayoutRes int itemLayoutRes, @Nullable Callbacks callback) {
        super(activity, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = songFiles;
        this.itemLayoutRes = itemLayoutRes;
        this.callbacks = callback;
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position).isDirectory() ? FOLDER : FILE;
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).hashCode();
    }

    public void swapDataSet(@NonNull List<File> songFiles) {
        this.dataSet = songFiles;
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        final File file = dataSet.get(index);

        if (holder.title != null) {
            holder.title.setText(getFileTitle(file));
        }
        if (holder.text != null) {
            if (holder.getItemViewType() == FILE) {
                holder.text.setText(getFileText(file));
            } else {
                holder.text.setVisibility(View.GONE);
            }
        }

        if (holder.image != null) {
            loadFileImage(file, holder);
        }
    }

    protected String getFileTitle(File file) {
        return file.getName();
    }

    protected String getFileText(File file) {
        return file.isDirectory() ? null : readableFileSize(file.length());
    }

    @SuppressWarnings("ConstantConditions")
    protected void loadFileImage(File file, final ViewHolder holder) {
//        final int iconColor = ATHUtil.INSTANCE.resolveColor(activity, R.attr.iconColor);
        if (file.isDirectory()) {
//            holder.image.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            holder.image.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
            holder.image.setScaleType(ImageView.ScaleType.CENTER);
            holder.image.setImageResource(R.drawable.ic_keyboard_arrow_right);
        } else {
            GlideLoader.with(activity)
                    .load(new AudioFileCover(file.getName(), file.getPath(), file.lastModified()))
                    .into(holder.image);
        }
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return size + " B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected File getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(File object) {
        return getFileTitle(object);
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, List<File> selection) {
        if (callbacks == null) return;
        callbacks.onMultipleItemAction(menuItem, selection);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(dataSet.get(position).getName().charAt(0)).toUpperCase();
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            if (menu != null && callbacks != null) {
                menu.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (isPositionInRange(position)) {
                        callbacks.onFileMenuClicked(dataSet.get(position), v);
                    }
                });
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            callbacks.onFileSelected(dataSet.get(position));
        }

        private boolean isPositionInRange(int position) {
            return position >= 0 && position < dataSet.size();
        }
    }

    public interface Callbacks {
        void onFileSelected(File file);

        void onFileMenuClicked(File file, View view);

        void onMultipleItemAction(MenuItem item, List<File> files);
    }
}
