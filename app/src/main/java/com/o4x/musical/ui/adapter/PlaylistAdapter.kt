package com.o4x.musical.ui.adapter

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import com.o4x.musical.R
import com.o4x.musical.extensions.toPlaylistDetail
import com.o4x.musical.helper.menu.PlaylistMenuHelper.handleMenuClick
import com.o4x.musical.helper.menu.PlaylistMenuHelper.handleMultipleItemAction
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Playlist
import com.o4x.musical.model.smartplaylist.AbsSmartPlaylist
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.adapter.base.AbsMultiSelectAdapter
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder
import com.o4x.musical.util.MusicUtil

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class PlaylistAdapter(
    val activity: MainActivity,
    var dataSet: List<Playlist>,
    @param:LayoutRes var itemLayoutRes: Int,
    cabHolder: CabHolder?
) : AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist>(
    activity, cabHolder, R.menu.menu_playlists_selection
) {

    companion object {
        private const val SMART_PLAYLIST = 0
        private const val DEFAULT_PLAYLIST = 1
    }

    init {
        setHasStableIds(true)
    }

    fun swapDataSet(dataSet: List<Playlist>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(
            itemLayoutRes, parent, false
        )
        return createViewHolder(view, viewType)
    }

    fun createViewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = dataSet[position]
        holder.itemView.isActivated = isChecked(playlist)

        holder.title?.text = playlist.name

        playlist.getSongsLive().observe(holder.itemView.context as LifecycleOwner, {
            holder.text?.text = MusicUtil.getSongCountString(activity, it.size)
            GlideLoader.with(activity).load(playlist, it).into(holder.image)
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
        ) R.drawable.ic_favorite_white_24dp else R.drawable.ic_queue_music_white_24dp
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataSet[position] is AbsSmartPlaylist) SMART_PLAYLIST else DEFAULT_PLAYLIST
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): Playlist? {
        return dataSet[position]
    }

    override fun getName(playlist: Playlist): String {
        return playlist.name
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
                activity.navController.toPlaylistDetail(playlist)
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
}