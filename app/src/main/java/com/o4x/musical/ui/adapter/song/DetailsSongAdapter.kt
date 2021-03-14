package com.o4x.musical.ui.adapter.song

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import butterknife.BindView
import com.o4x.appthemehelper.extensions.withAlpha
import com.google.android.material.textview.MaterialTextView
import com.o4x.musical.R
import com.o4x.musical.helper.GridHelper
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Song
import com.o4x.musical.ui.adapter.album.HorizontalAlbumAdapter
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.helper.MyPalette
import com.o4x.musical.util.NavigationUtil
import com.o4x.musical.util.color.MediaNotificationProcessor

class DetailsSongAdapter(
    activity: AppCompatActivity?,
    dataSet: List<Song?>?,
    @LayoutRes itemLayoutRes: Int,
    val cabHolder: CabHolder?,
    data: Any,
    colors: MediaNotificationProcessor
) : SongAdapter(activity, dataSet, itemLayoutRes, cabHolder) {

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
                                subHeaderView.setOnClickListener { _ ->
                                    NavigationUtil.goToArtist(
                                        activity,
                                        it.artistId
                                    )
                                }
                                hTrackCount.setText(
                                    MusicUtil.getSongCountString(
                                        activity,
                                        it.songCount
                                    )
                                )
                            }
                            is Artist -> {
                                hTitle.text = it.name
                                hSubtitle.text =
                                    MusicUtil.getAlbumCountString(activity, it.albumCount)
                                hTrackCount.text =
                                    MusicUtil.getSongCountString(activity, it.songCount)
                                hAlbumRecyclerView.visibility = View.VISIBLE
                                hAlbumAdapter?.swapDataSet(it.albums)
                                hAlbumAdapter?.colors = colors
                            }
                            else -> {}
                        }
                    }

                    colors?.let {
                        hTitle.setTextColor(it.primaryTextColor)
                        hSubtitle.setTextColor(it.secondaryTextColor)
                        hGradient.backgroundTintList = ColorStateList.valueOf(it.backgroundColor)
                        headerView.setBackgroundColor(it.backgroundColor)
                        setLineColor(it.secondaryTextColor)
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
                            val trackNumberString =
                                if (trackNumber > 0) trackNumber.toString() else "-"
                            it.text = trackNumberString
                        }
                    }
                    is Artist -> {
                    }
                }

                colors?.let {
                    holder.apply {
                        title?.setTextColor(it.primaryTextColor)
                        text?.setTextColor(it.secondaryTextColor)
                        imageText?.setTextColor(it.secondaryTextColor)
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
        @BindView(R.id.sub_header)
        lateinit var subHeaderView: View
        @BindView(R.id.gradient)
        lateinit var hGradient: View
        @BindView(R.id.title)
        lateinit var hTitle: MaterialTextView
        @BindView(R.id.subtitle)
        lateinit var hSubtitle: MaterialTextView
        @BindView(R.id.album_recycler)
        lateinit var hAlbumRecyclerView: RecyclerView
        @BindView(R.id.track_count)
        lateinit var hTrackCount: MaterialTextView
        @BindView(R.id.left_line)
        lateinit var hLeftLine: View
        @BindView(R.id.right_line)
        lateinit var hRightLine: View

        var hAlbumAdapter: HorizontalAlbumAdapter? = null

        init {
            hAlbumRecyclerView.layoutManager = GridHelper.linearLayoutManager(activity)
            hAlbumAdapter = HorizontalAlbumAdapter(activity, ArrayList(), cabHolder, colors!!)
            hAlbumRecyclerView.adapter = hAlbumAdapter
            hAlbumAdapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()
                    if (hAlbumAdapter!!.itemCount == 0) activity.finish()
                }
            })
        }

        fun setLineColor(color: Int) {
            hTrackCount.setTextColor(color.withAlpha(.7f))
            hLeftLine.setBackgroundColor(color.withAlpha(.7f))
            hRightLine.setBackgroundColor(color.withAlpha(.7f))
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

    override fun loadImage(song: Song, holder: SongAdapter.ViewHolder) {
        if (data is Artist) {
            super.loadImage(song, holder)
        }
    }
}