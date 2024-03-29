package github.o4x.musical.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import github.o4x.musical.R;
import github.o4x.musical.model.CategoryInfo;
import github.o4x.musical.util.SwipeAndDragHelper;

import java.util.List;

public class CategoryInfoAdapter extends RecyclerView.Adapter<CategoryInfoAdapter.ViewHolder> implements SwipeAndDragHelper.ActionCompletionContract {
    private List<CategoryInfo> categoryInfos;
    private ItemTouchHelper touchHelper;

    public CategoryInfoAdapter(List<CategoryInfo> categoryInfos) {
        this.categoryInfos = categoryInfos;
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(this);
        touchHelper = new ItemTouchHelper(swipeAndDragHelper);
    }

    @Override
    @NonNull
    public CategoryInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_dialog_library_categories_listitem, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull CategoryInfoAdapter.ViewHolder holder, int position) {
        CategoryInfo categoryInfo = categoryInfos.get(position);

        holder.checkBox.setChecked(categoryInfo.isVisible());
        holder.title.setText(holder.title.getResources().getString(categoryInfo.getCategory().getStringRes()));

        holder.itemView.setOnClickListener(v -> {
            if (!(categoryInfo.isVisible() && isLastCheckedCategory(categoryInfo))) {
                categoryInfo.setVisible(!categoryInfo.isVisible());
                holder.checkBox.setChecked(categoryInfo.isVisible());
            } else {
                Toast.makeText(holder.itemView.getContext(), R.string.you_have_to_select_at_least_one_category, Toast.LENGTH_SHORT).show();
            }
        });

        holder.dragView.setOnTouchListener((view, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        touchHelper.startDrag(holder);
                    }
                    return false;
                }
        );
    }

    @Override
    public int getItemCount() {
        return categoryInfos.size();
    }

    public void setCategoryInfos(List<CategoryInfo> categoryInfos) {
        this.categoryInfos = categoryInfos;
        notifyDataSetChanged();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        CategoryInfo categoryInfo = categoryInfos.get(oldPosition);
        categoryInfos.remove(oldPosition);
        categoryInfos.add(newPosition, categoryInfo);
        notifyItemMoved(oldPosition, newPosition);
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        touchHelper.attachToRecyclerView(recyclerView);
    }

    public List<CategoryInfo> getCategoryInfos() {
        return categoryInfos;
    }

    private boolean isLastCheckedCategory(CategoryInfo categoryInfo) {
        if (categoryInfo.isVisible()) {
            for (CategoryInfo c : categoryInfos) {
                if (c != categoryInfo && c.isVisible()) return false;
            }
        }
        return true;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView title;
        public View dragView;

        public ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkbox);
            title = view.findViewById(R.id.album_name);
            dragView = view.findViewById(R.id.drag_view);
        }
    }
}

