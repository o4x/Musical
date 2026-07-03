package github.o4x.m2.ui.adapter

import android.view.View
import androidx.annotation.LayoutRes
import github.o4x.m2.extensions.toGenreDetail
import github.o4x.m2.helper.SortOrder
import github.o4x.m2.model.Genre
import github.o4x.m2.model.Song
import github.o4x.m2.repository.GenreRepository
import github.o4x.m2.ui.activities.MainActivity
import github.o4x.m2.ui.adapter.base.AbsAdapter
import github.o4x.m2.ui.adapter.base.MediaEntryViewHolder
import github.o4x.m2.util.MusicUtil
import github.o4x.m2.prefs.PreferenceUtil.genreSortOrder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class GenreAdapter(
    val mainActivity: MainActivity,
    dataSet: List<Genre?>?,
    @LayoutRes itemLayoutRes: Int
) : AbsAdapter<GenreAdapter.ViewHolder, Genre>(mainActivity, dataSet, itemLayoutRes), KoinComponent {

    private val genreRepository: GenreRepository by inject()

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
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
            if (genre != null) songs.addAll(genreRepository.songs(genre.id))
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
            val genre = dataSet[adapterPosition]!!
            mainActivity.navController.toGenreDetail(genre)
        }

        init {
            if (menu != null) {
                menu!!.visibility = View.GONE
            }
        }
    }
}