package com.o4x.musical.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import butterknife.BindView
import butterknife.ButterKnife
import com.o4x.musical.R
import com.o4x.musical.interfaces.LoaderIds
import com.o4x.musical.loader.ArtistLoader
import com.o4x.musical.misc.WrappedAsyncTaskLoader
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Song
import com.o4x.musical.repository.RealAlbumRepository
import com.o4x.musical.repository.RealSongRepository
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import com.o4x.musical.ui.adapter.SearchAdapter
import com.o4x.musical.util.Util
import java.util.*

class SearchActivity : AbsMusicServiceActivity(), SearchView.OnQueryTextListener,
    LoaderManager.LoaderCallbacks<List<Any>> {
    @JvmField
    @BindView(R.id.recycler_view)
    var recyclerView: RecyclerView? = null

    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(android.R.id.empty)
    var empty: TextView? = null

    var searchView: SearchView? = null
    private var adapter: SearchAdapter? = null
    private var query: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setDrawUnderBar()
        ButterKnife.bind(this)
        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setTaskDescriptionColorAuto()
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        adapter = SearchAdapter(this, emptyList())
        adapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                empty!!.visibility = if (adapter!!.itemCount < 1) View.VISIBLE else View.GONE
            }
        })
        recyclerView!!.adapter = adapter
        recyclerView!!.setOnTouchListener { v: View?, event: MotionEvent? ->
            hideSoftKeyboard()
            false
        }
        setUpToolBar()
        if (savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY)
        }
        supportLoaderManager.initLoader(LOADER_ID, null, this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(QUERY, query)
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView
        searchView!!.queryHint = getString(R.string.search_hint)
        searchView!!.maxWidth = Int.MAX_VALUE
        searchItem.expandActionView()
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                onBackPressed()
                return false
            }
        })
        searchView!!.setQuery(query, false)
        searchView!!.post { searchView!!.setOnQueryTextListener(this@SearchActivity) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun search(query: String) {
        this.query = query
        supportLoaderManager.restartLoader(LOADER_ID, null, this)
    }

    override fun onMediaStoreChanged() {
        super.onMediaStoreChanged()
        supportLoaderManager.restartLoader(LOADER_ID, null, this)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        hideSoftKeyboard()
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        search(newText)
        return false
    }

    private fun hideSoftKeyboard() {
        Util.hideSoftKeyboard(this@SearchActivity)
        if (searchView != null) {
            searchView!!.clearFocus()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<Any>> {
        return AsyncSearchResultLoader(this, query) as Loader<List<Any>>
    }

    override fun onLoadFinished(loader: Loader<List<Any>>, data: List<Any>) {
        adapter!!.swapDataSet(data)
    }

    override fun onLoaderReset(loader: Loader<List<Any>>) {
        adapter!!.swapDataSet(emptyList())
    }

    private class AsyncSearchResultLoader(context: Context?, private val query: String?) :
        WrappedAsyncTaskLoader<List<Any?>?>(context) {
        override fun loadInBackground(): List<Any>? {
            val results: MutableList<Any> = ArrayList()
            if (!TextUtils.isEmpty(query)) {
                val songs: List<Song> = RealSongRepository(context).songs(query!!.trim { it <= ' ' })
                if (songs.isNotEmpty()) {
                    results.add(context.resources.getString(R.string.songs))
                    results.addAll(songs)
                }
                val artists: List<Artist> = ArtistLoader.getArtists(context, query.trim { it <= ' ' })
                if (artists.isNotEmpty()) {
                    results.add(context.resources.getString(R.string.artists))
                    results.addAll(artists)
                }
                val albums: List<Album> = RealAlbumRepository(RealSongRepository(context)).albums(query.trim { it <= ' ' })
                if (albums.isNotEmpty()) {
                    results.add(context.resources.getString(R.string.albums))
                    results.addAll(albums)
                }
            }
            return results
        }
    }

    companion object {
        const val QUERY = "query"
        private const val LOADER_ID = LoaderIds.SEARCH_ACTIVITY
    }
}