package com.o4x.musical.ui.adapter.album

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.o4x.musical.R
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Album
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.color.MediaNotificationProcessor

class HorizontalAlbumAdapter(
    activity: AppCompatActivity,
    dataSet: List<Album>,
    cabHolder: CabHolder?,
    colors: MediaNotificationProcessor
) : AlbumAdapter(activity, dataSet, R.layout.item_card_home, cabHolder) {

    var colors: MediaNotificationProcessor? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        this.colors = colors
    }

    override fun createViewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun setColors(color: Int, holder: ViewHolder) {}

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        colors?.let {
            holder.paletteColorContainer?.setBackgroundColor(it.actionBarColor)
            holder.title?.setTextColor(it.primaryTextColor)
            holder.text?.setTextColor(it.secondaryTextColor)
        }
    }

    override fun getAlbumText(album: Album): String? {
        return MusicUtil.getYearString(album.year)
    }
}