package com.o4x.musical.ui.adapter.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.o4x.musical.R
import com.o4x.musical.model.Song

class HomeAdapter(
    val activity: AppCompatActivity,
    val viewLifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    companion object {
        const val PADDING = 0
        const val QUEUE  = 1337
        const val OTHER  = 1338
    }

    var dataSet = mapOf<Int, MutableList<Song>>()

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return PADDING
        return when (dataSet.keys.toIntArray()[position]) {
            R.string.playing_queue -> QUEUE
            else -> OTHER
        }
    }

    private fun getItemLayoutRe(viewType: Int): Int {
        return when(viewType) {
            PADDING -> R.layout.item_home_padding
            QUEUE -> R.layout.item_home_queue
            else -> R.layout.item_home
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(activity)
            .inflate(getItemLayoutRe(viewType), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        var count = 1
        for (i in dataSet) {
            if (i.value.isNotEmpty()) count += 1
        }
        return count
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {}
}