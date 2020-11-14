package com.o4x.musical.ui.adapter

import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import com.o4x.musical.R
import com.o4x.musical.extensions.toPlaylistDetail
import com.o4x.musical.helper.menu.PlaylistMenuHelper.handleMenuClick
import com.o4x.musical.helper.menu.PlaylistMenuHelper.handleMultipleItemAction
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Playlist
import com.o4x.musical.model.Song
import com.o4x.musical.model.smartplaylist.AbsSmartPlaylist
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.adapter.base.AbsAdapter
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder
import com.o4x.musical.util.MusicUtil

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class PlaylistAdapter(
    val mainActivity: MainActivity,
    dataSet: List<Playlist>,
    @param:LayoutRes var itemLayoutRes: Int,
    cabHolder: CabHolder?
) : AbsAdapter<PlaylistAdapter.ViewHolder, Playlist>(
    mainActivity, dataSet, itemLayoutRes, cabHolder, R.menu.menu_playlists_selection
) {

    companion object {
        private const val SMART_PLAYLIST = 0
        private const val DEFAULT_PLAYLIST = 1
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun createViewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = dataSet[position]

        holder.title?.text = playlist.name

        playlist.getSongsLive().observe(holder.itemView.context as LifecycleOwner, {
            holder.text?.text = MusicUtil.getSongCountString(activity, it.size)
            getImageLoader().load(playlist, it).into(holder.image)
        })


//        holder.image?.setImageResource(getIconRes(playlist))
    }

    private fun getIconRes(playlist: Playlist): Int {
        if (playlist is AbsSmartPlaylist) {
            return playlist.iconRes
        }
        return if (MusicUtil.isFavoritePlaylist(
                activity,
                playlist
            )
        ) R.drawable.ic_star else R.drawable.ic_queue_music
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataSet[position] is AbsSmartPlaylist) SMART_PLAYLIST else DEFAULT_PLAYLIST
    }

    override fun getSectionName(position: Int): String {
        return  ""
    }

    override fun getName(`object`: Playlist): String {
        return `object`.name
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: MutableList<Playlist>) {
        handleMultipleItemAction(activity, selection, menuItem)
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {
        override fun onClick(view: View) {
            if (isInQuickSelectMode) {
                toggleChecked(adapterPosition)
            } else {
                val playlist = dataSet[adapterPosition]
                mainActivity.navController.toPlaylistDetail(playlist)
            }
        }

        override fun onLongClick(view: View): Boolean {
            toggleChecked(adapterPosition)
            return true
        }

        init {

            if (menu != null) {
                menu!!.setOnClickListener { view: View? ->
                    val playlist = dataSet[adapterPosition]
                    val popupMenu = PopupMenu(activity, view)
                    popupMenu.inflate(if (getItemViewType() == SMART_PLAYLIST) R.menu.menu_item_smart_playlist else R.menu.menu_item_playlist)
                    popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                        handleMenuClick(
                            activity, playlist, item!!
                        )
                    }
                    popupMenu.show()
                }
            }
        }
    }

    override fun loadImage(data: Playlist?, holder: ViewHolder?) {}
    override fun getSongList(data: MutableList<Playlist>): MutableList<Song> {
        return mutableListOf()
    }
}