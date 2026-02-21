package github.o4x.musical.ui.adapter.album

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import github.o4x.musical.R
import github.o4x.musical.model.Album
import github.o4x.musical.util.MusicUtil
import github.o4x.musical.util.color.MediaNotificationProcessor

class HorizontalAlbumAdapter(
    activity: AppCompatActivity,
    dataSet: List<Album>,
    colors: MediaNotificationProcessor
) : AlbumAdapter(activity, dataSet, R.layout.item_card_home) {

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