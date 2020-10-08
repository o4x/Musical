package com.o4x.musical.ui.adapter.song

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.appthemehelper.util.ATHUtil.resolveColor
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Song
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.color.MediaNotificationProcessor

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class DetailsSongAdapter(
    activity: AppCompatActivity?,
    dataSet: List<Song?>?,
    @LayoutRes itemLayoutRes: Int,
    usePalette: Boolean,
    cabHolder: CabHolder?
) : SongAdapter(activity, dataSet, itemLayoutRes, usePalette, cabHolder) {

    var colors: MediaNotificationProcessor? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun createViewHolder(view: View): SongAdapter.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val song = dataSet[position]

        holder.imageText?.let {
            val trackNumber = MusicUtil.getFixedTrackNumber(song.trackNumber)
            val trackNumberString = if (trackNumber > 0) trackNumber.toString() else "-"
            it.text = trackNumberString
        }

        colors?.let {
            holder.apply {
                title?.setTextColor(it.primaryTextColor)
                text?.setTextColor(it.secondaryTextColor)
                imageText?.setTextColor(it.secondaryTextColor)
                shortSeparator?.setBackgroundColor(
                    ColorUtil.withAlpha(it.secondaryTextColor, 0.3f)
                )
                menu?.setColorFilter(it.secondaryTextColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    override fun getSongText(song: Song): String {
        return MusicUtil.getReadableDurationString(song.duration)
    }

    inner class ViewHolder(itemView: View) : SongAdapter.ViewHolder(itemView) {
        init {
            imageText?.visibility = View.VISIBLE
            image?.visibility = View.GONE
        }
    }

    override fun loadAlbumCover(song: Song, holder: SongAdapter.ViewHolder) {
        // We don't want to load it in this adapter
    }
}