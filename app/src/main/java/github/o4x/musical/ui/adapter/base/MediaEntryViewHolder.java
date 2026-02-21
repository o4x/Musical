package github.o4x.musical.ui.adapter.base;

import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import github.o4x.musical.R;
import github.o4x.musical.util.ColorExtKt;
import github.o4x.musical.views.IconImageView;


public class MediaEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    @Nullable
    public ImageView image;

    @Nullable
    public IconImageView icon;

    @Nullable
    public TextView imageText;

    @Nullable
    public TextView title;

    @Nullable
    public TextView text;

    @Nullable
    public IconImageView menu;

    @Nullable
    public View dragView;

    @Nullable
    public View paletteColorContainer;

    public MediaEntryViewHolder(View itemView) {
        super(itemView);

        // Replaced ButterKnife.bind with findViewById
        image = itemView.findViewById(R.id.image);
        icon = itemView.findViewById(R.id.icon);
        imageText = itemView.findViewById(R.id.track_number);
        title = itemView.findViewById(R.id.album_name);
        text = itemView.findViewById(R.id.text);
        menu = itemView.findViewById(R.id.menu);
        dragView = itemView.findViewById(R.id.drag_view);
        paletteColorContainer = itemView.findViewById(R.id.palette_color_container);

        if (paletteColorContainer != null) {
            paletteColorContainer.setBackgroundColor(ColorExtKt.cardColor(itemView.getContext()));
        }

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
        // Todo: Implement click logic
    }
}
