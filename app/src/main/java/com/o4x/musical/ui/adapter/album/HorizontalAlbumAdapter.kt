package com.o4x.musical.ui.adapter.album

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.appthemehelper.util.ColorUtil.isColorLight
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getPrimaryTextColor
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getSecondaryTextColor
import com.o4x.musical.helper.HorizontalAdapterHelper
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Album
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.color.MediaNotificationProcessor

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class HorizontalAlbumAdapter(
    activity: AppCompatActivity,
    dataSet: List<Album>,
    cabHolder: CabHolder?,
    colors: MediaNotificationProcessor
) : AlbumAdapter(activity, dataSet, HorizontalAdapterHelper.LAYOUT_RES, cabHolder) {

    var colors: MediaNotificationProcessor? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        this.colors = colors
    }

    override fun createViewHolder(view: View, viewType: Int): ViewHolder {
        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        HorizontalAdapterHelper.applyMarginToLayoutParams(activity, params, viewType)
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

    override fun getItemViewType(position: Int): Int {
        return HorizontalAdapterHelper.getItemViewtype(position, itemCount)
    }
}