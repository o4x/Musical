package com.o4x.musical.ui.adapter.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.o4x.musical.R
import com.o4x.musical.model.Song
import com.o4x.musical.ui.fragments.mainactivity.home.HomeFragment

class HomeAdapter(
    val homeFragment: HomeFragment,
    dataSet: MutableList<RecyclerItem>
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    companion object {
        const val PADDING = 0
        const val QUEUE  = 1337
        const val OTHER  = 1338
    }

    var dataSet: MutableList<RecyclerItem> = dataSet
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun getRealPosition(position: Int): Int {
        return position - 1
    }

    class RecyclerItem(
        @StringRes val title: Int,
        val adapter: HomeSongAdapter,
        val layoutManager: RecyclerView.LayoutManager
    )

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return PADDING
        if (dataSet[getRealPosition(position)].title == R.string.playing_queue)
            return QUEUE
        return OTHER
    }

    private fun getItemLayoutRe(viewType: Int): Int {
        return when(viewType) {
            PADDING -> R.layout.item_home_padding
            QUEUE -> R.layout.item_home_queue
            else -> R.layout.item_home
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(homeFragment.requireContext())
            .inflate(getItemLayoutRe(viewType), parent, false)
        return when(viewType) {
            PADDING -> ViewHolder(view)
            QUEUE -> QueueViewHolder(view)
            else -> OtherViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) != PADDING) {
            val item = dataSet[getRealPosition(position)]
            (holder as OtherViewHolder).apply {
                title.text = homeFragment.getText(item.title)
                recyclerView.layoutManager = item.layoutManager
                recyclerView.adapter = item.adapter
            }
        }
    }

    override fun getItemCount(): Int {
        return 1 + dataSet.size
    }

    open inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {}

    open inner class OtherViewHolder(view: View) : ViewHolder(view) {
        @BindView(R.id.recycler_view)
        lateinit var recyclerView: RecyclerView
        @BindView(R.id.title)
        lateinit var title: TextView

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    inner class QueueViewHolder(view: View) : OtherViewHolder(view) {}
}