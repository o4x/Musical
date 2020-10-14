package com.o4x.musical.ui.adapter.song

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import butterknife.BindView
import code.name.monkey.appthemehelper.util.ColorUtil
import com.google.android.material.textview.MaterialTextView
import com.o4x.musical.R
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Song
import com.o4x.musical.ui.adapter.album.HorizontalAlbumAdapter
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.color.MediaNotificationProcessor

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class DetailsSongAdapter(
    activity: AppCompatActivity?,
    dataSet: List<Song?>?,
    @LayoutRes itemLayoutRes: Int,
    usePalette: Boolean,
    val cabHolder: CabHolder?,
    data: Any,
    colors: MediaNotificationProcessor
) : SongAdapter(activity, dataSet, itemLayoutRes, usePalette, cabHolder) {

    companion object {
        private const val HEADER = 0
        private const val SONG = 1
    }

    var colors: MediaNotificationProcessor? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var data: Any? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    init {
        this.colors = colors
        this.data = data
    }

    private fun getDataSetPos(position: Int): Int {
        return position - 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER
        else SONG
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemId(position: Int): Long {
        return if (getItemViewType(position) == HEADER)
            -1
        else
            super.getItemId(getDataSetPos(position))
    }

    override fun getIdentifier(position: Int): Song? {
        return if (getItemViewType(position) == HEADER)
            null
        else
            super.getIdentifier(getDataSetPos(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapter.ViewHolder {
        return if (viewType == HEADER) HeaderViewHolder(
            LayoutInflater.from(
                activity
            ).inflate(R.layout.activity_detail_header, parent, false)
        ) else SongViewHolder(
            LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.apply {
                    data?.let {
                        when (it) {
                            is Album -> {
                                hTitle.text = it.title
                                hSubtitle.text = it.artistName
                                headerView.setOnClickListener { v ->

                                    NavigationUtil.goToArtist(
                                        activity,
                                        it.artistId
                                    )
                                }
                            }
                            is Artist -> {
                                hTitle.text = it.name
                                hSubtitle.text = MusicUtil.getReadableDurationString(
                                    MusicUtil.getTotalDuration(
                                        activity,
                                        it.songs
                                    )
                                )
                                hAlbumRecyclerView.setVisibility(View.VISIBLE)
                                hAlbumAdapter?.swapDataSet(it.albums)
                            }
                            else -> {
                            }
                        }
                    }

                    colors?.let {
                        hTitle.setTextColor(it.primaryTextColor)
                        hSubtitle.setTextColor(it.secondaryTextColor)
                        hGradient.setBackgroundTintList(ColorStateList.valueOf(it.backgroundColor))
                        headerView.setBackgroundColor(it.backgroundColor)
                    }
                }
            }
            is SongViewHolder -> {
                super.onBindViewHolder(holder, getDataSetPos(position))
                val song = dataSet[getDataSetPos(position)]

                when (data) {
                    is Album -> {
                        holder.imageText?.visibility = View.VISIBLE
                        holder.image?.visibility = View.GONE
                        holder.imageText?.let {
                            val trackNumber = MusicUtil.getFixedTrackNumber(song.trackNumber)
                            val trackNumberString = if (trackNumber > 0) trackNumber.toString() else "-"
                            it.text = trackNumberString
                        }
                    }
                    is Artist -> {}
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

                        itemView.setBackgroundColor(it.backgroundColor)
                    }
                }
            }
        }
    }

    override fun getSongText(song: Song): String {
        return MusicUtil.getReadableDurationString(song.duration)
    }

    inner class HeaderViewHolder(itemView: View) : SongAdapter.ViewHolder(itemView) {
        @BindView(R.id.header)
        lateinit var headerView: View
        @BindView(R.id.gradient)
        lateinit var hGradient: View
        @BindView(R.id.title)
        lateinit var hTitle: MaterialTextView
        @BindView(R.id.subtitle)
        lateinit var hSubtitle: MaterialTextView
        @BindView(R.id.album_recycler)
        lateinit var hAlbumRecyclerView: RecyclerView

        var hAlbumAdapter: HorizontalAlbumAdapter? = null

        init {
            hAlbumRecyclerView.setLayoutManager(
                LinearLayoutManager(
                    activity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            )
            hAlbumAdapter = HorizontalAlbumAdapter(activity, ArrayList(), true, cabHolder)
            hAlbumRecyclerView.setAdapter(hAlbumAdapter)
            hAlbumAdapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    if (hAlbumAdapter!!.getItemCount() == 0) activity.finish()
                }
            })
        }

        // We don't want to click in this holder
        override fun onClick(v: View?) {
        }
        override fun onLongClick(view: View?): Boolean {
            return true
        }
    }

    inner class SongViewHolder(itemView: View) : SongAdapter.ViewHolder(itemView) {
        override fun getRealPosition(): Int {
            return getDataSetPos(super.getRealPosition())
        }
    }

    override fun loadAlbumCover(song: Song, holder: SongAdapter.ViewHolder) {
        if (data is Artist) {
            super.loadAlbumCover(song, holder)
        }
    }
}