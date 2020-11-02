package com.o4x.musical.ui.activities.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import butterknife.BindView
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.ThemeStore.Companion.themeColor
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.o4x.musical.R
import com.o4x.musical.extensions.themeColor
import com.o4x.musical.helper.MusicPlayerRemote.openAndShuffleQueue
import com.o4x.musical.interfaces.CabCallback
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.Genre
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import com.o4x.musical.ui.adapter.song.SongAdapter
import com.o4x.musical.ui.viewmodel.GenreDetailsViewModel
import com.o4x.musical.util.PhonographColorUtil
import com.o4x.musical.util.ViewUtil
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

class GenreDetailActivity : AbsMusicPanelActivity(), CabHolder {

    companion object {
        const val EXTRA_GENRE = "extra_genre"
    }

    private val detailsViewModel: GenreDetailsViewModel by viewModel {
        parametersOf(intent.extras!!.getParcelable(EXTRA_GENRE))
    }

    @BindView(R.id.recycler_view)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(android.R.id.empty)
    lateinit var empty: TextView

    private var genre: Genre? = null
    private var cab: AttachedCab? = null
    private var adapter: SongAdapter? = null
    private var wrappedAdapter: RecyclerView.Adapter<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setTaskDescriptionColorAuto()
        genre = intent.extras!!.getParcelable(EXTRA_GENRE)
        setUpRecyclerView()
        setUpToolBar()
        addMusicServiceEventListener(detailsViewModel)
        detailsViewModel.getSongs().observe(this, {
            adapter?.swapDataSet(it)
        })
    }

    override fun createContentView(): View? {
        return wrapSlidingMusicPanel(R.layout.activity_genre_detail)
    }

    private fun setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(
            this,
            recyclerView as FastScrollRecyclerView?,
            themeColor(this)
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SongAdapter(this, ArrayList(), R.layout.item_list, this)
        recyclerView.adapter = adapter
        adapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    private fun setUpToolBar() {
        toolbar.setBackgroundColor(themeColor(this))
        setSupportActionBar(toolbar)
        supportActionBar!!.title = genre!!.name
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_genre_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shuffle_genre -> {
                openAndShuffleQueue(adapter!!.dataSet, true)
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun openCab(menuRes: Int, callback: CabCallback): AttachedCab {
        if (cab != null && cab!!.isActive()) cab!!.destroy()
        cab = createCab(R.id.cab_stub) {
            menu(menuRes)
            closeDrawable(R.drawable.ic_close_white_24dp)
            backgroundColor(literal = themeColor())

            onCreate { cab, menu ->
                callback.onCreate(cab, menu)
            }
            onSelection { item ->
                return@onSelection callback.onSelection(item)
            }
            onDestroy { cab ->
                return@onDestroy callback.onDestroy(cab)
            }
        }
        return cab!!
    }

    override fun onBackPressed() {
        if (cab != null && cab!!.isActive()) cab!!.destroy() else {
            recyclerView.stopScroll()
            super.onBackPressed()
        }
    }

    private fun checkIsEmpty() {
        empty.visibility =
            if (adapter!!.itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        recyclerView.adapter = null
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
            wrappedAdapter = null
        }
        adapter = null
        super.onDestroy()
    }
}