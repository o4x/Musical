package com.o4x.musical.ui.adapter.album

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.o4x.musical.R
import com.o4x.musical.helper.SortOrder
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Album
import com.o4x.musical.model.Song
import com.o4x.musical.ui.adapter.base.AbsAdapter
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.prefs.PreferenceUtil.albumSortOrder
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
open class AlbumAdapter(
    activity: AppCompatActivity,
    dataSet: List<Album>,
    itemLayoutRes: Int,
    cabHolder: CabHolder?
) : AbsAdapter<AlbumAdapter.ViewHolder, Album>(
    activity, dataSet, itemLayoutRes, cabHolder
) {

    override fun createViewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(view)
    }

    fun getAlbumTitle(album: Album): String? {
        return album.title
    }

    protected open fun getAlbumText(album: Album): String? {
        return MusicUtil.buildInfoString(
            album.artistName,
            MusicUtil.getSongCountString(activity, album.songs.size)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val album = dataSet[position]

        if (holder.title != null) {
            holder.title!!.text = getAlbumTitle(album)
        }
        if (holder.text != null) {
            holder.text!!.text = getAlbumText(album)
        }
    }

    override fun loadImage(album: Album, holder: ViewHolder) {
        if (holder.image == null) return
        getImageLoader(holder)
            .load(album)
            .into(holder.image!!)
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun getName(album: Album?): String {
        return album?.title!!
    }

    override fun getSongList(albums: List<Album?>): List<Song> {
        val songs: MutableList<Song> = ArrayList()
        for (album in albums) {
            album?.songs?.let { songs.addAll(it) }
        }
        return songs
    }

    override fun getSectionName(position: Int): String {
        var sectionName: String? = null
        when (albumSortOrder) {
            SortOrder.AlbumSortOrder.ALBUM_A_Z, SortOrder.AlbumSortOrder.ALBUM_Z_A -> sectionName =
                dataSet[position].title
            SortOrder.AlbumSortOrder.ALBUM_ARTIST -> sectionName = dataSet[position].artistName
            SortOrder.AlbumSortOrder.ALBUM_YEAR -> return MusicUtil.getYearString(
                dataSet[position].year
            )
        }
        return MusicUtil.getSectionName(sectionName)
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {
        override fun onClick(v: View) {
            if (isInQuickSelectMode) {
                toggleChecked(adapterPosition)
            } else {
                NavigationUtil.goToAlbum(activity, dataSet[adapterPosition].id)
            }
        }

        override fun onLongClick(view: View): Boolean {
            toggleChecked(adapterPosition)
            return true
        }

        init {
            setImageTransitionName(activity.getString(R.string.transition_album_art))
            if (menu != null) {
                menu!!.visibility = View.GONE
            }
        }
    }
}