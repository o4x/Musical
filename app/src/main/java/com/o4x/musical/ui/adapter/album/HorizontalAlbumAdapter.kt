package com.o4x.musical.ui.adapter.album

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import code.name.monkey.appthemehelper.util.ColorUtil.isColorLight
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getPrimaryTextColor
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getSecondaryTextColor
import com.o4x.musical.helper.HorizontalAdapterHelper
import com.o4x.musical.imageloader.universalil.listener.PaletteImageLoadingListener
import com.o4x.musical.imageloader.universalil.loader.UniversalIL
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Album
import com.o4x.musical.util.MusicUtil

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class HorizontalAlbumAdapter(
    activity: AppCompatActivity,
    dataSet: List<Album>,
    usePalette: Boolean,
    cabHolder: CabHolder?
) : AlbumAdapter(activity, dataSet, HorizontalAdapterHelper.LAYOUT_RES, usePalette, cabHolder) {

    override fun createViewHolder(view: View, viewType: Int): ViewHolder {
        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        HorizontalAdapterHelper.applyMarginToLayoutParams(activity, params, viewType)
        return ViewHolder(view)
    }

    override fun setColors(color: Int, holder: ViewHolder) {
        if (holder.itemView != null) {
            val card = holder.itemView as CardView
            card.setCardBackgroundColor(color)
            if (holder.title != null) {
                holder.title!!.setTextColor(getPrimaryTextColor(activity, isColorLight(color)))
            }
            if (holder.text != null) {
                holder.text!!.setTextColor(getSecondaryTextColor(activity, isColorLight(color)))
            }
        }
    }

    override fun loadAlbumCover(album: Album, holder: ViewHolder) {
        if (holder.image == null) return
        UniversalIL(
            holder.image!!,
            object : PaletteImageLoadingListener() {
                override fun onColorReady(color: Int) {
                    if (usePalette) setColors(color, holder) else setColors(
                        getAlbumArtistFooterColor(activity),
                        holder
                    )
                }
            }).loadImage(album.safeGetFirstSong())
    }

    override fun getAlbumText(album: Album): String? {
        return MusicUtil.getYearString(album.year)
    }

    override fun getItemViewType(position: Int): Int {
        return HorizontalAdapterHelper.getItemViewtype(position, itemCount)
    }
}