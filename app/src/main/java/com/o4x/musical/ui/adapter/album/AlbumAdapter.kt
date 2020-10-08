package com.o4x.musical.ui.adapter.album

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.appthemehelper.util.ColorUtil.isColorLight
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getPrimaryTextColor
import code.name.monkey.appthemehelper.util.MaterialValueHelper.getSecondaryTextColor
import com.o4x.musical.R
import com.o4x.musical.helper.SortOrder
import com.o4x.musical.helper.menu.SongsMenuHelper
import com.o4x.musical.imageloader.universalil.listener.PaletteImageLoadingListener
import com.o4x.musical.imageloader.universalil.loader.UniversalIL
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Album
import com.o4x.musical.model.Song
import com.o4x.musical.ui.adapter.base.AbsMultiSelectAdapter
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.PreferenceUtil.albumSortOrder
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
open class AlbumAdapter(
    protected val activity: AppCompatActivity,
    var dataSet: List<Album>,
    @param:LayoutRes protected var itemLayoutRes: Int,
    usePalette: Boolean,
    cabHolder: CabHolder?
) : AbsMultiSelectAdapter<AlbumAdapter.ViewHolder?, Album?>(
    activity, cabHolder, R.menu.menu_media_selection
), SectionedAdapter {

    protected var usePalette = false

    fun usePalette(usePalette: Boolean) {
        this.usePalette = usePalette
        notifyDataSetChanged()
    }

    fun swapDataSet(dataSet: List<Album>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(
            itemLayoutRes, parent, false
        )
        return createViewHolder(view, viewType)
    }

    protected open fun createViewHolder(view: View, viewType: Int): ViewHolder {
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
        val album = dataSet[position]
        val isChecked = isChecked(album)
        holder.itemView.isActivated = isChecked
        if (holder.adapterPosition == itemCount - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator!!.visibility = View.GONE
            }
        } else {
            if (holder.shortSeparator != null) {
                holder.shortSeparator!!.visibility = View.VISIBLE
            }
        }
        if (holder.title != null) {
            holder.title!!.text = getAlbumTitle(album)
        }
        if (holder.text != null) {
            holder.text!!.text = getAlbumText(album)
        }
        loadAlbumCover(album, holder)
    }

    protected open fun setColors(color: Int, holder: ViewHolder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer!!.setBackgroundColor(color)
            if (holder.title != null) {
                holder.title!!.setTextColor(getPrimaryTextColor(activity, isColorLight(color)))
            }
            if (holder.text != null) {
                holder.text!!.setTextColor(getSecondaryTextColor(activity, isColorLight(color)))
            }
        }
    }

    protected open fun loadAlbumCover(album: Album, holder: ViewHolder) {
        if (holder.image == null) return
        UniversalIL(
            holder.image!!,
            object : PaletteImageLoadingListener() {
                override fun onColorReady(color: Int) {
                    if (usePalette) setColors(color, holder) else setColors(
                        getDefaultFooterColor(
                            activity
                        ), holder
                    )
                }
            }).loadImage(album.safeGetFirstSong())
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun getIdentifier(position: Int): Album? {
        return dataSet[position]
    }

    override fun getName(album: Album?): String {
        return album?.title!!
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<Album?>) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.itemId)
    }

    private fun getSongList(albums: List<Album?>): List<Song> {
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

    init {
        this.usePalette = usePalette
        setHasStableIds(true)
    }
}