package com.o4x.musical.ui.adapter

import android.view.View
import androidx.annotation.LayoutRes
import com.o4x.musical.extensions.toGenreDetail
import com.o4x.musical.helper.SortOrder
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Genre
import com.o4x.musical.model.Song
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.adapter.base.AbsAdapter
import com.o4x.musical.ui.adapter.base.MediaEntryViewHolder
import com.o4x.musical.util.MusicUtil
import com.o4x.musical.util.PreferenceUtil.genreSortOrder
import java.util.*

class GenreAdapter(
    val mainActivity: MainActivity,
    dataSet: List<Genre?>?,
    @LayoutRes itemLayoutRes: Int,
    cabHolder: CabHolder?
) : AbsAdapter<GenreAdapter.ViewHolder?, Genre?>(mainActivity, dataSet, itemLayoutRes, cabHolder) {

    override fun getItemId(position: Int): Long {
        return dataSet[position].hashCode().toLong()
    }

    override fun createViewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val genre = dataSet[position]!!
        if (holder.title != null) {
            holder.title!!.text = genre.name
        }
        if (holder.text != null) {
            holder.text!!.text = MusicUtil.getGenreInfoString(activity, genre)
        }
    }

    override fun loadImage(genre: Genre?, holder: ViewHolder?) {
        if (holder?.image == null) return
        getImageLoader(holder)
            .load(genre!!)
            .into(holder.image!!)
    }

    override fun getName(genre: Genre?): String? {
        return genre?.name
    }

    override fun getSongList(genres: List<Genre?>): List<Song> {
        val songs: MutableList<Song> = ArrayList()
        genres.forEach { genre ->
            songs.addAll(genre?.songs!!) // maybe async in future?
        }
        return songs
    }

    override fun getSectionName(position: Int): String {
        var sectionName: String? = null
        when (genreSortOrder) {
            SortOrder.GenreSortOrder.GENRE_A_Z, SortOrder.GenreSortOrder.GENRE_Z_A -> sectionName =
                dataSet[position]!!.name
        }
        return MusicUtil.getSectionName(sectionName)
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {
        override fun onClick(view: View) {
            if (isInQuickSelectMode) {
                toggleChecked(adapterPosition)
            } else {
                val genre = dataSet[adapterPosition]!!
                mainActivity.navController.toGenreDetail(genre)
            }
        }

        override fun onLongClick(view: View): Boolean {
            toggleChecked(adapterPosition)
            return true
        }

        init {
            if (menu != null) {
                menu!!.visibility = View.GONE
            }
        }
    }
}