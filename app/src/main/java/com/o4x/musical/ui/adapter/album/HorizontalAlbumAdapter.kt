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

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class HorizontalAlbumAdapter(
    activity: AppCompatActivity,
    dataSet: List<Album>,
    cabHolder: CabHolder?
) : AlbumAdapter(activity, dataSet, HorizontalAdapterHelper.LAYOUT_RES, cabHolder) {

    override fun createViewHolder(view: View, viewType: Int): ViewHolder {
        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        HorizontalAdapterHelper.applyMarginToLayoutParams(activity, params, viewType)
        return ViewHolder(view)
    }

    override fun setColors(color: Int, holder: ViewHolder) {
        holder.itemView.setBackgroundColor(color)
        if (holder.title != null) {
            holder.title!!.setTextColor(getPrimaryTextColor(activity, isColorLight(color)))
        }
        if (holder.text != null) {
            holder.text!!.setTextColor(getSecondaryTextColor(activity, isColorLight(color)))
        }
    }

    override fun getAlbumText(album: Album): String? {
        return MusicUtil.getYearString(album.year)
    }

    override fun getItemViewType(position: Int): Int {
        return HorizontalAdapterHelper.getItemViewtype(position, itemCount)
    }
}