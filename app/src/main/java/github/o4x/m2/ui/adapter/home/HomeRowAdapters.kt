package github.o4x.m2.ui.adapter.home

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import github.o4x.m2.R

/**
 * Spacer item that keeps the poster visible above the first home section.
 */
class HomeSpacerAdapter(
    private val spacerHeight: Int
) : RecyclerView.Adapter<HomeSpacerAdapter.ViewHolder>() {

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_header, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.updateLayoutParams { height = spacerHeight }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

/**
 * Base for home rows that appear only while their section has content.
 */
abstract class HomeSingleRowAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    var visible: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) notifyItemInserted(0) else notifyItemRemoved(0)
        }

    override fun getItemCount(): Int = if (visible) 1 else 0
}

/**
 * Clickable section title row, with an optional shuffle button.
 */
class HomeSectionTitleAdapter(
    @StringRes private val titleRes: Int,
    private val centerTitle: Boolean,
    private val onClick: () -> Unit,
    private val onShuffleClick: (() -> Unit)? = null
) : HomeSingleRowAdapter<HomeSectionTitleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_section_title, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.setText(titleRes)
        holder.title.updateLayoutParams<FrameLayout.LayoutParams> {
            gravity = if (centerTitle) {
                Gravity.CENTER
            } else {
                Gravity.START or Gravity.CENTER_VERTICAL
            }
        }
        holder.itemView.setOnClickListener { onClick() }
        holder.shuffleButton.isVisible = onShuffleClick != null
        holder.shuffleButton.setOnClickListener { onShuffleClick?.invoke() }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.section_title)
        val shuffleButton: MaterialButton = itemView.findViewById(R.id.section_shuffle_button)
    }
}

/**
 * Hosts the horizontal playing-queue recycler as a single full-width row that
 * is always present; while the queue is empty the row keeps the strip's height
 * and shows a centered empty message instead.
 */
class HomeQueueRowAdapter(
    private val queueRecyclerView: RecyclerView,
    private val rowHeight: Int
) : RecyclerView.Adapter<HomeQueueRowAdapter.ViewHolder>() {

    var showEmpty: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            notifyItemChanged(0)
        }

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_queue_row, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.updateLayoutParams { height = rowHeight }
        holder.emptyText.isVisible = showEmpty
        queueRecyclerView.isVisible = !showEmpty
        // The queue recycler is owned by the fragment so its adapter and scroll
        // position survive rebinds; just host it behind the empty label.
        if (queueRecyclerView.parent !== holder.itemView) {
            (queueRecyclerView.parent as? ViewGroup)?.removeView(queueRecyclerView)
            (holder.itemView as FrameLayout).addView(
                queueRecyclerView,
                0,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL
                )
            )
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emptyText: TextView = itemView.findViewById(R.id.queue_empty_text)
    }
}
