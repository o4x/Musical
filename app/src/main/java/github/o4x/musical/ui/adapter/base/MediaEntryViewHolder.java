package github.o4x.musical.ui.adapter.base;

import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import github.o4x.musical.R;
import github.o4x.musical.views.IconImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.o4x.appthemehelper.extensions.ColorExtKt;

public class MediaEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    @Nullable
    @BindView(R.id.image)
    public ImageView image;

    @Nullable
    @BindView(R.id.icon)
    public IconImageView icon;

    @Nullable
    @BindView(R.id.track_number)
    public TextView imageText;

    @Nullable
    @BindView(R.id.album_name)
    public TextView title;

    @Nullable
    @BindView(R.id.text)
    public TextView text;

    @Nullable
    @BindView(R.id.menu)
    public IconImageView menu;

    @Nullable
    @BindView(R.id.drag_view)
    public View dragView;

    @Nullable
    @BindView(R.id.palette_color_container)
    public View paletteColorContainer;

    public MediaEntryViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        if (paletteColorContainer != null)
            paletteColorContainer.setBackgroundColor(ColorExtKt.cardColor(itemView.getContext()));

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    protected void setImageTransitionName(@NonNull String transitionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && image != null) {
            image.setTransitionName(transitionName);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onClick(View v) {

    }
}
