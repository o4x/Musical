package com.o4x.musical.ui.adapter.song

import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Song

class SelectSongAdapter(
    activity: AppCompatActivity?,
    dataSet: List<Song?>?,
    @LayoutRes itemLayoutRes: Int,
    val cabHolder: CabHolder?,
    val onClick: (song: Song) -> Unit
) : SongAdapter(activity, dataSet, itemLayoutRes, cabHolder) {

    override fun createViewHolder(view: View, viewType: Int): SongAdapter.ViewHolder {
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View): SongAdapter.ViewHolder(itemView) {

        override fun onClick(v: View?) {
            this@SelectSongAdapter.onClick(dataSet[realPosition])
        }
    }

}